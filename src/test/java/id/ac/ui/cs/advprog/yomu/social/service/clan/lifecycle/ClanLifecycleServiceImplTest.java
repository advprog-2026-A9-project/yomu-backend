package id.ac.ui.cs.advprog.yomu.social.service.clan.lifecycle;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
@SuppressWarnings("PMD")
class ClanLifecycleServiceImplTest {

    private static final String CLAN_ID = "clan-123";
    private static final String CLAN_NAME = "Wibu Indo";
    private static final String NEW_CLAN_NAME = "Wibu Elite";
    private static final String CLAN_DESC = "Clan untuk pecinta manga";
    private static final String NEW_CLAN_DESC = "Clan untuk wibu elit";
    private static final String USERNAME = "user-123";
    private static final String OTHER_USER = "other-user";

    // Assertion Messages
    private static final String MSG_CLAN_NAME = "Clan name should match";
    private static final String MSG_CLAN_DESC = "Clan description should match";
    private static final String MSG_CLAN_LEADER = "Clan leader should match";
    private static final String MSG_CLAN_TIER = "Clan tier should match";
    private static final String MSG_EXCEPTION_MSG = "Exception message should match";

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ClanValidator clanValidator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ClanLifecycleServiceImpl lifecycleService;

    private ClanRequest createRequest;
    private Clan dummyClan;

    @BeforeEach
    void setUp() {
        createRequest = new ClanRequest();
        createRequest.setName(CLAN_NAME);
        createRequest.setDescription(CLAN_DESC);
        createRequest.setUsername(USERNAME);

        dummyClan = new Clan();
        dummyClan.setId(CLAN_ID);
        dummyClan.setName(CLAN_NAME);
        dummyClan.setDescription(CLAN_DESC);
        dummyClan.setLeaderUsername(USERNAME);
        dummyClan.setTier(Tier.BRONZE);
        dummyClan.setScore(0);
    }

    @Test
    void createClan_Success() {
        when(clanRepository.existsByName(CLAN_NAME)).thenReturn(false);
        when(memberRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(clanRepository.save(any(Clan.class))).thenAnswer(invocation -> {
            Clan saved = invocation.getArgument(0);
            saved.setId(CLAN_ID);
            return saved;
        });

        Clan created = lifecycleService.createClan(createRequest);

        assertAll("Verify created clan attributes",
                () -> assertNotNull(created, "Created clan should not be null"),
                () -> assertEquals(CLAN_NAME, created.getName(), MSG_CLAN_NAME),
                () -> assertEquals(CLAN_DESC, created.getDescription(), MSG_CLAN_DESC),
                () -> assertEquals(USERNAME, created.getLeaderUsername(), MSG_CLAN_LEADER),
                () -> assertEquals(Tier.BRONZE, created.getTier(), MSG_CLAN_TIER)
        );

        verify(clanValidator).requireValidClanName(CLAN_NAME);
        verify(clanValidator).requireValidClanDescription(CLAN_DESC);
        verify(clanValidator).requireClanNameAvailable(false);
        verify(clanValidator).requireNotMemberOfOtherClan(false);
        verify(eventPublisher).publishEvent(any(ClanCreatedEvent.class));
    }

    @Test
    void createClan_NullUsername_ThrowsNullPointerException() {
        createRequest.setUsername(null);

        assertThrows(NullPointerException.class, () ->
            lifecycleService.createClan(createRequest),
            "Should throw NullPointerException when username is null"
        );
    }

    @Test
    void editClan_Success_NoNameChange() {
        ClanRequest editRequest = new ClanRequest();
        editRequest.setName(CLAN_NAME);
        editRequest.setDescription(NEW_CLAN_DESC);

        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(clanRepository.save(any(Clan.class))).thenReturn(dummyClan);

        Clan edited = lifecycleService.editClan(CLAN_ID, USERNAME, editRequest);

        assertAll("Verify edited clan attributes",
                () -> assertNotNull(edited, "Edited clan should not be null"),
                () -> assertEquals(CLAN_NAME, edited.getName(), MSG_CLAN_NAME),
                () -> assertEquals(NEW_CLAN_DESC, edited.getDescription(), MSG_CLAN_DESC)
        );

        verify(clanValidator).requireClanId(CLAN_ID);
        verify(clanValidator).requireLeaderPrivilege(dummyClan, USERNAME, "Hanya Leader yang dapat mengubah info clan");
        verify(clanValidator).requireValidClanName(CLAN_NAME);
        verify(clanValidator).requireValidClanDescription(NEW_CLAN_DESC);
        verify(clanRepository, never()).existsByName(any());
        verify(eventPublisher, never()).publishEvent(any(ClanNameChangedEvent.class));
    }

    @Test
    void editClan_Success_WithNameChange() {
        ClanRequest editRequest = new ClanRequest();
        editRequest.setName(NEW_CLAN_NAME);
        editRequest.setDescription(NEW_CLAN_DESC);

        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(clanRepository.existsByName(NEW_CLAN_NAME)).thenReturn(false);
        when(clanRepository.save(any(Clan.class))).thenReturn(dummyClan);

        Clan edited = lifecycleService.editClan(CLAN_ID, USERNAME, editRequest);

        assertAll("Verify edited clan attributes with name change",
                () -> assertNotNull(edited, "Edited clan should not be null"),
                () -> assertEquals(NEW_CLAN_NAME, edited.getName(), MSG_CLAN_NAME),
                () -> assertEquals(NEW_CLAN_DESC, edited.getDescription(), MSG_CLAN_DESC)
        );

        verify(clanValidator).requireClanNameAvailable(false);
        verify(eventPublisher).publishEvent(any(ClanNameChangedEvent.class));
    }

    @Test
    void editClan_NotFound_ThrowsIllegalArgumentException() {
        ClanRequest editRequest = new ClanRequest();
        editRequest.setName(CLAN_NAME);

        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            lifecycleService.editClan(CLAN_ID, USERNAME, editRequest),
            "Should throw IllegalArgumentException when clan to edit not found"
        );

        assertEquals("Clan tidak ditemukan.", ex.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void deleteClan_Success() {
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));

        lifecycleService.deleteClan(CLAN_ID, USERNAME);

        verify(clanValidator).requireClanId(CLAN_ID);
        verify(clanValidator).requireLeaderPrivilege(dummyClan, USERNAME, "Hanya Leader yang dapat menghapus clan");
        verify(clanRepository).delete(dummyClan);
        verify(eventPublisher).publishEvent(any(UserDeleteClanEvent.class));
    }

    @Test
    void deleteClan_NotFound_ThrowsIllegalArgumentException() {
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            lifecycleService.deleteClan(CLAN_ID, USERNAME),
            "Should throw IllegalArgumentException when clan to delete not found"
        );

        assertEquals("Clan tidak ditemukan.", ex.getMessage(), MSG_EXCEPTION_MSG);
    }
}
