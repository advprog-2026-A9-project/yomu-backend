package id.ac.ui.cs.advprog.yomu.social.service.clan.joinrequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanJoinRequestResponse;
import id.ac.ui.cs.advprog.yomu.social.event.JoinRequestAcceptedEvent;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequest;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequestStatus;
import id.ac.ui.cs.advprog.yomu.social.port.ClanLookupPort;
import id.ac.ui.cs.advprog.yomu.social.port.ClanMemberValidationPort;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanJoinRequestRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ClanJoinRequestServiceImplTest {

    private static final String CLAN_ID = "clan-123";
    private static final String CLAN_NAME = "Wibu Elite";
    private static final String LEADER_ID = "leader-123";
    private static final String USERNAME = "wibu-member";
    private static final Long REQUEST_ID = 42L;

    // Assertion messages
    private static final String MSG_STATUS = "Request status should match";
    private static final String MSG_THROW = "Should throw IllegalArgumentException";
    private static final String MSG_PAGE_SIZE = "Page size should match";
    private static final String MSG_PAGE_CONTENT = "Page content should match";

    @Mock private ClanLookupPort clanLookup;
    @Mock private ClanMemberValidationPort memberValidation;
    @Mock private ClanJoinRequestRepository joinRequestRepository;
    @Mock private ClanValidator clanValidator;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ClanJoinRequestServiceImpl joinRequestService;

    private Clan dummyClan;
    private ClanJoinRequest dummyRequest;

    @BeforeEach
    void setUp() {
        dummyClan = new Clan();
        dummyClan.setId(CLAN_ID);
        dummyClan.setName(CLAN_NAME);
        dummyClan.setLeaderUsername(LEADER_ID);

        dummyRequest = new ClanJoinRequest();
        dummyRequest.setId(REQUEST_ID);
        dummyRequest.setClanId(CLAN_ID);
        dummyRequest.setUsername(USERNAME);
        dummyRequest.setStatus(ClanJoinRequestStatus.PENDING);
        dummyRequest.setCreatedAt(LocalDateTime.now());
    }

    // ─── requestJoin ─────────────────────────────────────────────────────────

    @Test
    void requestJoin_WhenValid_ShouldSaveRequest() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        doNothing().when(clanValidator).requireUsername(USERNAME);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberValidation.existsByClanIdAndUsername(CLAN_ID, USERNAME)).thenReturn(false);
        when(memberValidation.existsByUsername(USERNAME)).thenReturn(false);
        when(joinRequestRepository.findByClanIdAndUsernameAndStatus(CLAN_ID, USERNAME, ClanJoinRequestStatus.PENDING))
                .thenReturn(Optional.empty());

        joinRequestService.requestJoin(CLAN_ID, USERNAME);

        assertAll("Verify request is validated and saved",
                () -> verify(clanValidator).requireNotAlreadyMember(false),
                () -> verify(clanValidator).requireNotMemberOfOtherClan(false),
                () -> verify(joinRequestRepository).save(any(ClanJoinRequest.class))
        );
    }

    @Test
    void requestJoin_WhenClanNotFound_ShouldThrow() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        doNothing().when(clanValidator).requireUsername(USERNAME);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, 
                () -> joinRequestService.requestJoin(CLAN_ID, USERNAME), 
                MSG_THROW);
    }

    @Test
    void requestJoin_WhenAlreadyMember_ShouldThrow() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        doNothing().when(clanValidator).requireUsername(USERNAME);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberValidation.existsByClanIdAndUsername(CLAN_ID, USERNAME)).thenReturn(true);
        doThrow(new IllegalArgumentException("Already member"))
                .when(clanValidator).requireNotAlreadyMember(true);

        assertThrows(IllegalArgumentException.class, 
                () -> joinRequestService.requestJoin(CLAN_ID, USERNAME), 
                MSG_THROW);
    }

    @Test
    void requestJoin_WhenMemberOfOtherClan_ShouldThrow() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        doNothing().when(clanValidator).requireUsername(USERNAME);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberValidation.existsByClanIdAndUsername(CLAN_ID, USERNAME)).thenReturn(false);
        when(memberValidation.existsByUsername(USERNAME)).thenReturn(true);
        doThrow(new IllegalArgumentException("Member of other clan"))
                .when(clanValidator).requireNotMemberOfOtherClan(true);

        assertThrows(IllegalArgumentException.class, 
                () -> joinRequestService.requestJoin(CLAN_ID, USERNAME), 
                MSG_THROW);
    }

    @Test
    void requestJoin_WhenAlreadyHasPendingRequest_ShouldThrow() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        doNothing().when(clanValidator).requireUsername(USERNAME);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberValidation.existsByClanIdAndUsername(CLAN_ID, USERNAME)).thenReturn(false);
        when(memberValidation.existsByUsername(USERNAME)).thenReturn(false);
        when(joinRequestRepository.findByClanIdAndUsernameAndStatus(CLAN_ID, USERNAME, ClanJoinRequestStatus.PENDING))
                .thenReturn(Optional.of(dummyRequest));

        assertThrows(IllegalArgumentException.class, 
                () -> joinRequestService.requestJoin(CLAN_ID, USERNAME), 
                MSG_THROW);
    }

    // ─── getJoinRequests ─────────────────────────────────────────────────────

    @Test
    void getJoinRequests_WhenValid_ShouldReturnPage() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        doNothing().when(clanValidator).requireLeaderPrivilege(eq(dummyClan), eq(LEADER_ID), anyString());

        Pageable pageable = PageRequest.of(0, 10);
        Page<ClanJoinRequest> page = new PageImpl<>(List.of(dummyRequest), pageable, 1);
        when(joinRequestRepository.findByClanIdAndStatus(CLAN_ID, ClanJoinRequestStatus.PENDING, pageable))
                .thenReturn(page);

        Page<ClanJoinRequestResponse> result = joinRequestService.getJoinRequests(CLAN_ID, LEADER_ID, 0, 10);

        assertAll("Verify page attributes and contents",
                () -> assertNotNull(result, "Result should not be null"),
                () -> assertEquals(1, result.getTotalElements(), MSG_PAGE_SIZE),
                () -> assertEquals(USERNAME, result.getContent().get(0).username(), MSG_PAGE_CONTENT)
        );
    }

    @Test
    void getJoinRequests_WhenNotLeader_ShouldThrow() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        doThrow(new IllegalArgumentException("Not leader"))
                .when(clanValidator).requireLeaderPrivilege(eq(dummyClan), eq(USERNAME), anyString());

        assertThrows(IllegalArgumentException.class, 
                () -> joinRequestService.getJoinRequests(CLAN_ID, USERNAME, 0, 10), 
                MSG_THROW);
    }

    // ─── acceptJoinRequest ───────────────────────────────────────────────────

    @Test
    void acceptJoinRequest_WhenValid_ShouldSetStatusAcceptedAndPublishEvent() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        doNothing().when(clanValidator).requireLeaderPrivilege(eq(dummyClan), eq(LEADER_ID), anyString());
        when(joinRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(dummyRequest));
        when(memberValidation.existsByClanIdAndUsername(CLAN_ID, USERNAME)).thenReturn(false);
        when(memberValidation.existsByUsername(USERNAME)).thenReturn(false);
        when(memberValidation.countByClanId(CLAN_ID)).thenReturn(10L);

        joinRequestService.acceptJoinRequest(CLAN_ID, REQUEST_ID, LEADER_ID);

        assertAll("Verify accept status and publishing",
                () -> assertEquals(ClanJoinRequestStatus.ACCEPTED, dummyRequest.getStatus(), MSG_STATUS),
                () -> verify(joinRequestRepository).save(dummyRequest),
                () -> verify(eventPublisher).publishEvent(any(JoinRequestAcceptedEvent.class))
        );
    }

    @Test
    void acceptJoinRequest_WhenRequestNotFound_ShouldThrow() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(joinRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, 
                () -> joinRequestService.acceptJoinRequest(CLAN_ID, REQUEST_ID, LEADER_ID), 
                MSG_THROW);
    }

    @Test
    void acceptJoinRequest_WhenRequestNotForClan_ShouldThrow() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        dummyRequest.setClanId("different-clan");
        when(joinRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(dummyRequest));

        assertThrows(IllegalArgumentException.class, 
                () -> joinRequestService.acceptJoinRequest(CLAN_ID, REQUEST_ID, LEADER_ID), 
                MSG_THROW);
    }

    @Test
    void acceptJoinRequest_WhenRequestNotPending_ShouldThrow() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        dummyRequest.setStatus(ClanJoinRequestStatus.ACCEPTED);
        when(joinRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(dummyRequest));

        assertThrows(IllegalArgumentException.class, 
                () -> joinRequestService.acceptJoinRequest(CLAN_ID, REQUEST_ID, LEADER_ID), 
                MSG_THROW);
    }

    @Test
    void acceptJoinRequest_WhenAlreadyMemberOnAccept_ShouldThrow() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(joinRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(dummyRequest));
        when(memberValidation.existsByClanIdAndUsername(CLAN_ID, USERNAME)).thenReturn(true);
        doThrow(new IllegalArgumentException("Already member"))
                .when(clanValidator).requireNotAlreadyMember(true);

        assertThrows(IllegalArgumentException.class, 
                () -> joinRequestService.acceptJoinRequest(CLAN_ID, REQUEST_ID, LEADER_ID), 
                MSG_THROW);
    }

    @Test
    void acceptJoinRequest_WhenMemberOfOtherClanOnAccept_ShouldThrow() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(joinRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(dummyRequest));
        when(memberValidation.existsByClanIdAndUsername(CLAN_ID, USERNAME)).thenReturn(false);
        when(memberValidation.existsByUsername(USERNAME)).thenReturn(true);
        doThrow(new IllegalArgumentException("Member of other clan"))
                .when(clanValidator).requireNotMemberOfOtherClan(true);

        assertThrows(IllegalArgumentException.class, 
                () -> joinRequestService.acceptJoinRequest(CLAN_ID, REQUEST_ID, LEADER_ID), 
                MSG_THROW);
    }

    @Test
    void acceptJoinRequest_WhenClanFull_ShouldThrow() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(joinRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(dummyRequest));
        when(memberValidation.existsByClanIdAndUsername(CLAN_ID, USERNAME)).thenReturn(false);
        when(memberValidation.existsByUsername(USERNAME)).thenReturn(false);
        when(memberValidation.countByClanId(CLAN_ID)).thenReturn(50L);
        doThrow(new IllegalArgumentException("Clan is full"))
                .when(clanValidator).requireClanNotFull(50L);

        assertThrows(IllegalArgumentException.class, 
                () -> joinRequestService.acceptJoinRequest(CLAN_ID, REQUEST_ID, LEADER_ID), 
                MSG_THROW);
    }

    // ─── rejectJoinRequest ───────────────────────────────────────────────────

    @Test
    void rejectJoinRequest_WhenValid_ShouldSetStatusRejected() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        doNothing().when(clanValidator).requireLeaderPrivilege(eq(dummyClan), eq(LEADER_ID), anyString());
        when(joinRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(dummyRequest));

        joinRequestService.rejectJoinRequest(CLAN_ID, REQUEST_ID, LEADER_ID);

        assertAll("Verify request is updated to rejected and saved",
                () -> assertEquals(ClanJoinRequestStatus.REJECTED, dummyRequest.getStatus(), MSG_STATUS),
                () -> verify(joinRequestRepository).save(dummyRequest)
        );
    }

    // ─── rejectAllJoinRequests ───────────────────────────────────────────────

    @Test
    void rejectAllJoinRequests_WhenValid_ShouldUpdateStatusOfAllPendingRequests() {
        doNothing().when(clanValidator).requireClanId(CLAN_ID);
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        doNothing().when(clanValidator).requireLeaderPrivilege(eq(dummyClan), eq(LEADER_ID), anyString());

        joinRequestService.rejectAllJoinRequests(CLAN_ID, LEADER_ID);

        verify(joinRequestRepository).updateStatusByClanIdAndStatus(
                CLAN_ID, ClanJoinRequestStatus.PENDING, ClanJoinRequestStatus.REJECTED);
    }
}
