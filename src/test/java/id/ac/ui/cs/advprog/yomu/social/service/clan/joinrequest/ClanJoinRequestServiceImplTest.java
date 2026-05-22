package id.ac.ui.cs.advprog.yomu.social.service.clan.joinrequest;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
=======
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
>>>>>>> origin/staging
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
<<<<<<< HEAD
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
=======
import org.mockito.InjectMocks;
>>>>>>> origin/staging
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanJoinRequestResponse;
=======

>>>>>>> origin/staging
import id.ac.ui.cs.advprog.yomu.social.event.JoinRequestAcceptedEvent;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequest;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequestStatus;
<<<<<<< HEAD
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
=======
>>>>>>> origin/staging
import id.ac.ui.cs.advprog.yomu.social.port.ClanLookupPort;
import id.ac.ui.cs.advprog.yomu.social.port.ClanMemberValidationPort;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanJoinRequestRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;

@ExtendWith(MockitoExtension.class)
<<<<<<< HEAD
@SuppressWarnings("null")
class ClanJoinRequestServiceImplTest {

    private static final String CLAN_123 = "clan-123";
    private static final String LEADER_1 = "leader-1";
    private static final String USER_1 = "user-1";

    private static final String MSG_THROW = "Should throw IllegalArgumentException";
    private static final String MSG_MESSAGE = "Message should match";
=======
class ClanJoinRequestServiceImplTest {

    private static final String CLAN_ID = "clan-1";
    private static final String LEADER_ID = "leader-1";
    private static final String REQUESTER_USERNAME = "user-requester";
    private static final Long REQUEST_ID = 42L;
>>>>>>> origin/staging

    @Mock
    private ClanLookupPort clanLookup;

    @Mock
    private ClanMemberValidationPort memberValidation;

    @Mock
    private ClanJoinRequestRepository joinRequestRepository;

    @Mock
    private ClanValidator clanValidator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

<<<<<<< HEAD
=======
    @InjectMocks
>>>>>>> origin/staging
    private ClanJoinRequestServiceImpl joinRequestService;

    private Clan dummyClan;
    private ClanJoinRequest dummyRequest;

    @BeforeEach
    void setUp() {
<<<<<<< HEAD
        joinRequestService = new ClanJoinRequestServiceImpl(clanLookup, memberValidation, joinRequestRepository, clanValidator, eventPublisher);

        dummyClan = new Clan();
        dummyClan.setId(CLAN_123);
        dummyClan.setName("Wibu Elite");
        dummyClan.setLeaderUsername(LEADER_1);
        dummyClan.setTier(Tier.BRONZE);

        dummyRequest = new ClanJoinRequest();
        dummyRequest.setId(1L);
        dummyRequest.setClanId(CLAN_123);
        dummyRequest.setUsername(USER_1);
        dummyRequest.setStatus(ClanJoinRequestStatus.PENDING);
        dummyRequest.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void requestJoin_WhenClanNotFound_ShouldThrowException() {
        when(clanLookup.findClanById(CLAN_123)).thenReturn(Optional.empty());

        assertAll("Verify exception when clan not found",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> joinRequestService.requestJoin(CLAN_123, USER_1),
                            MSG_THROW);
                    assertEquals(SocialConstants.CLAN_NOT_FOUND_MESSAGE, ex.getMessage(), MSG_MESSAGE);
                });
    }

    @Test
    void requestJoin_WhenAlreadyHasPendingRequest_ShouldThrowException() {
        when(clanLookup.findClanById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(memberValidation.existsByClanIdAndUsername(CLAN_123, USER_1)).thenReturn(false);
        when(memberValidation.existsByUsername(USER_1)).thenReturn(false);
        when(joinRequestRepository.findByClanIdAndUsernameAndStatus(CLAN_123, USER_1, ClanJoinRequestStatus.PENDING))
                .thenReturn(Optional.of(dummyRequest));

        assertAll("Verify exception when already requested join",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> joinRequestService.requestJoin(CLAN_123, USER_1),
                            MSG_THROW);
                    assertEquals(SocialConstants.ALREADY_REQUESTED_JOIN_MESSAGE, ex.getMessage(), MSG_MESSAGE);
                });
    }

    @Test
    void requestJoin_WhenValid_ShouldSaveRequest() {
        when(clanLookup.findClanById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(memberValidation.existsByClanIdAndUsername(CLAN_123, USER_1)).thenReturn(false);
        when(memberValidation.existsByUsername(USER_1)).thenReturn(false);
        when(joinRequestRepository.findByClanIdAndUsernameAndStatus(CLAN_123, USER_1, ClanJoinRequestStatus.PENDING))
                .thenReturn(Optional.empty());
        when(joinRequestRepository.save(any(ClanJoinRequest.class))).thenReturn(dummyRequest);

        assertAll("Verify valid join request is saved",
                () -> assertDoesNotThrow(() -> joinRequestService.requestJoin(CLAN_123, USER_1),
                        "Should successfully request join without throwing exception"),
                () -> verify(clanValidator).requireClanId(CLAN_123),
                () -> verify(clanValidator).requireUsername(USER_1),
                () -> verify(clanValidator).requireNotAlreadyMember(false),
                () -> verify(clanValidator).requireNotMemberOfOtherClan(false),
                () -> verify(joinRequestRepository).save(any(ClanJoinRequest.class)));
    }

    @Test
    void getJoinRequests_ShouldReturnPage() {
        when(clanLookup.findClanById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(joinRequestRepository.findByClanIdAndStatus(CLAN_123, ClanJoinRequestStatus.PENDING, pageRequest))
                .thenReturn(new PageImpl<>(List.of(dummyRequest)));

        Page<ClanJoinRequestResponse> result = joinRequestService.getJoinRequests(CLAN_123, LEADER_1, 0, 10);

        assertAll("Verify page content",
                () -> assertEquals(1, result.getTotalElements(), "Total elements should be 1"),
                () -> assertEquals(USER_1, result.getContent().get(0).username(), "Username should match"),
                () -> verify(clanValidator).requireLeaderPrivilege(eq(dummyClan), eq(LEADER_1), anyString()));
    }

    @Test
    void acceptJoinRequest_WhenRequestNotFound_ShouldThrowException() {
        when(clanLookup.findClanById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(joinRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertAll("Verify exception when join request not found",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> joinRequestService.acceptJoinRequest(CLAN_123, 1L, LEADER_1),
                            MSG_THROW);
                    assertEquals(SocialConstants.REQUEST_NOT_FOUND_MESSAGE, ex.getMessage(), MSG_MESSAGE);
                });
    }

    @Test
    void acceptJoinRequest_WhenRequestForDifferentClan_ShouldThrowException() {
        when(clanLookup.findClanById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        dummyRequest.setClanId("different-clan");
        when(joinRequestRepository.findById(1L)).thenReturn(Optional.of(dummyRequest));

        assertAll("Verify exception when request belongs to a different clan",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> joinRequestService.acceptJoinRequest(CLAN_123, 1L, LEADER_1),
                            MSG_THROW);
                    assertEquals(SocialConstants.REQUEST_INVALID_MESSAGE, ex.getMessage(), MSG_MESSAGE);
                });
    }

    @Test
    void acceptJoinRequest_WhenValid_ShouldAcceptAndPublishEvent() {
        when(clanLookup.findClanById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(joinRequestRepository.findById(1L)).thenReturn(Optional.of(dummyRequest));
        when(memberValidation.existsByClanIdAndUsername(CLAN_123, USER_1)).thenReturn(false);
        when(memberValidation.existsByUsername(USER_1)).thenReturn(false);
        when(memberValidation.countByClanId(CLAN_123)).thenReturn(5L);

        joinRequestService.acceptJoinRequest(CLAN_123, 1L, LEADER_1);

        assertAll("Verify accept join request updates",
                () -> assertEquals(ClanJoinRequestStatus.ACCEPTED, dummyRequest.getStatus(), "Request status should be ACCEPTED"),
                () -> verify(joinRequestRepository).save(dummyRequest),
                () -> verify(clanValidator).requireNotAlreadyMember(false),
                () -> verify(clanValidator).requireNotMemberOfOtherClan(false),
                () -> verify(clanValidator).requireClanNotFull(5L),
                () -> verify(eventPublisher).publishEvent(any(JoinRequestAcceptedEvent.class)));
    }

    @Test
    void rejectJoinRequest_WhenValid_ShouldReject() {
        when(clanLookup.findClanById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(joinRequestRepository.findById(1L)).thenReturn(Optional.of(dummyRequest));

        joinRequestService.rejectJoinRequest(CLAN_123, 1L, LEADER_1);

        assertAll("Verify request is rejected",
                () -> assertEquals(ClanJoinRequestStatus.REJECTED, dummyRequest.getStatus(), "Request status should be REJECTED"),
                () -> verify(joinRequestRepository).save(dummyRequest));
    }

    @Test
    void rejectAllJoinRequests_ShouldCallRepositoryUpdate() {
        when(clanLookup.findClanById(CLAN_123)).thenReturn(Optional.of(dummyClan));

        assertAll("Verify reject all requests calls repository",
                () -> assertDoesNotThrow(() -> joinRequestService.rejectAllJoinRequests(CLAN_123, LEADER_1),
                        "Should successfully reject all requests without throwing exception"),
                () -> verify(joinRequestRepository).updateStatusByClanIdAndStatus(CLAN_123, ClanJoinRequestStatus.PENDING, ClanJoinRequestStatus.REJECTED));
    }
=======
        dummyClan = new Clan();
        dummyClan.setId(CLAN_ID);
        dummyClan.setLeaderUsername(LEADER_ID);
        dummyClan.setName("Wibu Elite");

        dummyRequest = new ClanJoinRequest();
        dummyRequest.setId(REQUEST_ID);
        dummyRequest.setClanId(CLAN_ID);
        dummyRequest.setUsername(REQUESTER_USERNAME);
        dummyRequest.setStatus(ClanJoinRequestStatus.PENDING);
    }

    @SuppressWarnings("null")
    @Test
    void acceptJoinRequest_WhenValid_ShouldSetStatusAcceptedAndPublishEvent() {
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(joinRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(dummyRequest));
        when(memberValidation.existsByClanIdAndUsername(CLAN_ID, REQUESTER_USERNAME)).thenReturn(false);
        when(memberValidation.existsByUsername(REQUESTER_USERNAME)).thenReturn(false);
        when(memberValidation.countByClanId(CLAN_ID)).thenReturn(5L);

        joinRequestService.acceptJoinRequest(CLAN_ID, REQUEST_ID, LEADER_ID);

        assertAll("Verify accept request behavior",
                () -> assertEquals(ClanJoinRequestStatus.ACCEPTED, dummyRequest.getStatus(),
                        "Request status should be updated to accepted"),
                () -> verify(joinRequestRepository).save(dummyRequest),
                () -> verify(eventPublisher).publishEvent(any(JoinRequestAcceptedEvent.class)));
    }
>>>>>>> origin/staging
}
