package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
@SuppressWarnings("null")
class AchievementShowcaseServiceImplTest {

    private static final String USER_ID = "user-123";
    private List<String> achievementIds;

    @Mock
    private UserAchievementShowcaseRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AchievementShowcaseServiceImpl showcaseService;

    @BeforeEach
    void setUp() {
        achievementIds = List.of("ach-1", "ach-2", "ach-3");
    }

    @Test
    void getShowcaseByUserId_WhenShowcaseExists_ShouldReturnList() {
        UserAchievementShowcase showcase = UserAchievementShowcase.builder()
                .userId(USER_ID)
                .achievementIds(achievementIds)
                .build();

        when(repository.findById(USER_ID)).thenReturn(Optional.of(showcase));

        List<String> result = showcaseService.getShowcaseByUserId(USER_ID);

        assertEquals(achievementIds, result, "Should return the exact list of achievement IDs stored in user showcase");
    }

    @Test
    void getShowcaseByUserId_WhenShowcaseDoesNotExist_ShouldReturnEmptyList() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());

        List<String> result = showcaseService.getShowcaseByUserId(USER_ID);

        assertEquals(List.of(), result, "Should return an empty list when user showcase does not exist");
    }

    @Test
    void updateShowcase_ShouldSaveToRepository() {
        ShowcaseUpdateRequest request = new ShowcaseUpdateRequest();
        request.setUserId(USER_ID);
        request.setAchievementIds(achievementIds);

        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        when(repository.save(any(UserAchievementShowcase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        showcaseService.updateShowcase(request);

        verify(repository).save(any(UserAchievementShowcase.class));
    }

    @Test
    void updateShowcase_ShouldPublishShowcaseChangedEvent() {
        ShowcaseUpdateRequest request = new ShowcaseUpdateRequest();
        request.setUserId(USER_ID);
        request.setAchievementIds(achievementIds);

        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        when(repository.save(any(UserAchievementShowcase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        showcaseService.updateShowcase(request);

        verify(eventPublisher).publishEvent(new UserShowcaseAchievementChangedEvent(USER_ID, achievementIds));
    }
}
