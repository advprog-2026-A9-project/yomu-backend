package id.ac.ui.cs.advprog.yomu.social.service.clan.lifecycle;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.event.ClanCreatedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.ClanNameChangedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserDeleteClanEvent;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ClanLifecycleServiceImplTest {

    private static final String CLAN_123 = "clan-123";
    private static final String WIBU_ELITE = "Wibu Elite";
    private static final String LEADER_1 = "leader-1";

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ClanValidator clanValidator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ClanLifecycleServiceImpl lifecycleService;

    private Clan dummyClan;
    private ClanRequest request;

    @BeforeEach
    void setUp() {
        lifecycleService = new ClanLifecycleServiceImpl(clanRepository, memberRepository, clanValidator, eventPublisher);

        dummyClan = new Clan();
        dummyClan.setId(CLAN_123);
        dummyClan.setName(WIBU_ELITE);
        dummyClan.setDescription("Descr");
        dummyClan.setLeaderUsername(LEADER_1);
        dummyClan.setTier(Tier.BRONZE);
        dummyClan.setScore(0);

        request = new ClanRequest();
        request.setName(WIBU_ELITE);
        request.setDescription("Descr");
        request.setUsername(LEADER_1);
    }

    @Test
    void createClan_WhenValid_ShouldCreateAndPublishEvent() {
        when(clanRepository.existsByName(request.getName())).thenReturn(false);
        when(memberRepository.findByUsername(LEADER_1)).thenReturn(Optional.empty());
        when(clanRepository.save(any(Clan.class))).thenReturn(dummyClan);

        Clan result = lifecycleService.createClan(request);

        assertAll("Verify created clan details",
                () -> assertNotNull(result, "Resulting clan should not be null"),
                () -> assertEquals(CLAN_123, result.getId(), "Clan ID should match"),
                () -> verify(clanValidator).requireValidClanName(request.getName()),
                () -> verify(clanValidator).requireValidClanDescription(request.getDescription()),
                () -> verify(clanValidator).requireClanNameAvailable(false),
                () -> verify(clanValidator).requireNotMemberOfOtherClan(false),
                () -> verify(eventPublisher).publishEvent(any(ClanCreatedEvent.class)));
    }

    @Test
    void createClan_WhenUsernameNull_ShouldThrowException() {
        request.setUsername(null);
        assertThrows(NullPointerException.class, () -> lifecycleService.createClan(request),
                "Should throw NullPointerException when username is null");
    }

    @Test
    void editClan_WhenClanNotFound_ShouldThrowException() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.empty());

        assertAll("Verify edit clan not found exception",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> lifecycleService.editClan(CLAN_123, LEADER_1, request),
                            "Should throw IllegalArgumentException");
                    assertEquals(SocialConstants.CLAN_NOT_FOUND_MESSAGE, ex.getMessage(), "Message should match");
                });
    }

    @Test
    void editClan_WhenNameChanged_ShouldValidateAvailabilityAndPublishEvent() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(clanRepository.existsByName("New Name")).thenReturn(false);
        when(clanRepository.save(any(Clan.class))).thenAnswer(inv -> inv.getArgument(0));

        request.setName("New Name");
        Clan result = lifecycleService.editClan(CLAN_123, LEADER_1, request);

        assertAll("Verify updated clan name",
                () -> assertEquals("New Name", result.getName(), "Name should match updated request"),
                () -> verify(clanValidator).requireClanId(CLAN_123),
                () -> verify(clanValidator).requireLeaderPrivilege(eq(dummyClan), eq(LEADER_1), anyString()),
                () -> verify(clanValidator).requireClanNameAvailable(false),
                () -> verify(eventPublisher).publishEvent(any(ClanNameChangedEvent.class)));
    }

    @Test
    void editClan_WhenNameNotChanged_ShouldNotValidateAvailabilityOrPublishEvent() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(clanRepository.save(any(Clan.class))).thenAnswer(inv -> inv.getArgument(0));

        request.setName(WIBU_ELITE); // Same name
        Clan result = lifecycleService.editClan(CLAN_123, LEADER_1, request);

        assertAll("Verify updated clan fields",
                () -> assertEquals(WIBU_ELITE, result.getName(), "Name should remain unchanged"),
                () -> verify(clanValidator, never()).requireClanNameAvailable(anyBoolean()),
                () -> verify(eventPublisher, never()).publishEvent(any(ClanNameChangedEvent.class)));
    }

    @Test
    void deleteClan_WhenClanNotFound_ShouldThrowException() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.empty());

        assertAll("Verify delete clan not found exception",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> lifecycleService.deleteClan(CLAN_123, LEADER_1),
                            "Should throw IllegalArgumentException");
                    assertEquals(SocialConstants.CLAN_NOT_FOUND_MESSAGE, ex.getMessage(), "Message should match");
                });
    }

    @Test
    void deleteClan_WhenValid_ShouldDeleteAndPublishEvent() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        doNothing().when(clanRepository).delete(dummyClan);

        assertAll("Verify clan deletion",
                () -> assertDoesNotThrow(() -> lifecycleService.deleteClan(CLAN_123, LEADER_1),
                        "Should delete clan without throwing any exception"),
                () -> verify(clanValidator).requireClanId(CLAN_123),
                () -> verify(clanValidator).requireLeaderPrivilege(eq(dummyClan), eq(LEADER_1), anyString()),
                () -> verify(clanRepository).delete(dummyClan),
                () -> verify(eventPublisher).publishEvent(any(UserDeleteClanEvent.class)));
    }
}
