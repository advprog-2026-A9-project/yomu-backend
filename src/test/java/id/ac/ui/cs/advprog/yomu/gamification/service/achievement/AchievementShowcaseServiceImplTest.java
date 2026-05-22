package id.ac.ui.cs.advprog.yomu.gamification.service.achievement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import id.ac.ui.cs.advprog.yomu.gamification.dto.ShowcaseUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.event.UserShowcaseAchievementChangedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementShowcase;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementShowcaseRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "PMD"})
class AchievementShowcaseServiceImplTest {

    private static final String USER_ID = "user-123";

    // Assertion Messages
    private static final String MSG_LIST_MATCH = "Should return the exact list of achievement IDs";
    private static final String MSG_EMPTY_LIST = "Should return an empty list";
    private static final String MSG_SAVE_CALLED = "Save should be called on repository";
    private static final String MSG_EVENT_CALLED = "Event publisher should be called";

    private List<String> achievementIds;

    @Mock
    private UserAchievementShowcaseRepository repository;

    @Mock
    private AchievementService achievementService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AchievementShowcaseServiceImpl showcaseService;

    @BeforeEach
    void setUp() {
        achievementIds = List.of("ach-1", "ach-2", "ach-3");
    }

    @Test
    void getShowcaseByUsername_WhenShowcaseExists_ShouldReturnList() {
        UserAchievementShowcase showcase = UserAchievementShowcase.builder()
                .username(USER_ID)
                .achievementIds(achievementIds)
                .build();

        when(repository.findById(USER_ID)).thenReturn(Optional.of(showcase));

        List<String> result = showcaseService.getShowcaseByUsername(USER_ID);

        assertEquals(achievementIds, result, MSG_LIST_MATCH);
    }

    @Test
    void getShowcaseByUsername_WhenShowcaseDoesNotExist_ShouldReturnEmptyList() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());

        List<String> result = showcaseService.getShowcaseByUsername(USER_ID);

        assertEquals(List.of(), result, MSG_EMPTY_LIST);
    }

    @Test
    void updateShowcase_WhenShowcaseDoesNotExist_ShouldCreateAndSave() {
        ShowcaseUpdateRequest request = new ShowcaseUpdateRequest();
        request.setUsername(USER_ID);
        request.setAchievementIds(achievementIds);

        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        when(repository.save(any(UserAchievementShowcase.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(achievementService.getAchievementsByIds(achievementIds)).thenReturn(List.of());

        showcaseService.updateShowcase(request);

        assertAll("Verify update showcase behavior on empty",
                () -> verify(repository).save(any(UserAchievementShowcase.class)),
                () -> verify(eventPublisher).publishEvent(any(UserShowcaseAchievementChangedEvent.class)));
    }

    @Test
    void updateShowcase_WhenShowcaseExists_ShouldUpdateAndSave() {
        ShowcaseUpdateRequest request = new ShowcaseUpdateRequest();
        request.setUsername(USER_ID);
        request.setAchievementIds(achievementIds);

        UserAchievementShowcase existingShowcase = UserAchievementShowcase.builder()
                .username(USER_ID)
                .achievementIds(new ArrayList<>())
                .build();

        when(repository.findById(USER_ID)).thenReturn(Optional.of(existingShowcase));
        when(repository.save(any(UserAchievementShowcase.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(achievementService.getAchievementsByIds(achievementIds)).thenReturn(List.of());

        showcaseService.updateShowcase(request);

        assertAll("Verify update showcase behavior on existing",
                () -> assertEquals(achievementIds, existingShowcase.getAchievementIds(), MSG_LIST_MATCH),
                () -> verify(repository).save(existingShowcase),
                () -> verify(eventPublisher).publishEvent(any(UserShowcaseAchievementChangedEvent.class)));
    }

    @Test
    void updateShowcase_WhenAchievementIdsNull_ShouldUseEmptyList() {
        ShowcaseUpdateRequest request = new ShowcaseUpdateRequest();
        request.setUsername(USER_ID);
        request.setAchievementIds(null);

        UserAchievementShowcase existingShowcase = UserAchievementShowcase.builder()
                .username(USER_ID)
                .achievementIds(achievementIds)
                .build();

        when(repository.findById(USER_ID)).thenReturn(Optional.of(existingShowcase));
        when(repository.save(any(UserAchievementShowcase.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(achievementService.getAchievementsByIds(any())).thenReturn(List.of());

        showcaseService.updateShowcase(request);

        assertAll("Verify update showcase behavior when ids are null",
                () -> assertEquals(List.of(), existingShowcase.getAchievementIds(), MSG_EMPTY_LIST),
                () -> verify(repository).save(existingShowcase),
                () -> verify(eventPublisher).publishEvent(any(UserShowcaseAchievementChangedEvent.class)));
    }
}
