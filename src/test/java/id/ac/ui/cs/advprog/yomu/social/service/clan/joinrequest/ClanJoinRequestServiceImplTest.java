package id.ac.ui.cs.advprog.yomu.social.service.clan.joinrequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.event.JoinRequestAcceptedEvent;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequest;
import id.ac.ui.cs.advprog.yomu.social.port.ClanLookupPort;
import id.ac.ui.cs.advprog.yomu.social.port.ClanMemberValidationPort;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanJoinRequestRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;

@ExtendWith(MockitoExtension.class)
class ClanJoinRequestServiceImplTest {

    private static final String CLAN_ID = "clan-1";
    private static final String LEADER_ID = "leader-1";
    private static final String REQUESTER_USERNAME = "user-requester";
    private static final Long REQUEST_ID = 42L;

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

    @InjectMocks
    private ClanJoinRequestServiceImpl joinRequestService;

    private Clan dummyClan;
    private ClanJoinRequest dummyRequest;

    @BeforeEach
    void setUp() {
        dummyClan = new Clan();
        dummyClan.setId(CLAN_ID);
        dummyClan.setLeaderUsername(LEADER_ID);
        dummyClan.setName("Wibu Elite");

        dummyRequest = new ClanJoinRequest();
        dummyRequest.setId(REQUEST_ID);
        dummyRequest.setClanId(CLAN_ID);
        dummyRequest.setUsername(REQUESTER_USERNAME);
        dummyRequest.setStatus(SocialConstants.REQUEST_STATUS_PENDING);
    }

    @Test
    void acceptJoinRequest_WhenValid_ShouldSetStatusAcceptedAndPublishEvent() {
        when(clanLookup.findClanById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(joinRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(dummyRequest));
        when(memberValidation.existsByClanIdAndUsername(CLAN_ID, REQUESTER_USERNAME)).thenReturn(false);
        when(memberValidation.existsByUsername(REQUESTER_USERNAME)).thenReturn(false);
        when(memberValidation.countByClanId(CLAN_ID)).thenReturn(5L);

        joinRequestService.acceptJoinRequest(CLAN_ID, REQUEST_ID, LEADER_ID);

        assertAll("Verify accept request behavior",
                () -> assertEquals(SocialConstants.REQUEST_STATUS_ACCEPTED, dummyRequest.getStatus(),
                        "Request status should be updated to accepted"),
                () -> verify(joinRequestRepository).save(dummyRequest),
                () -> verify(eventPublisher).publishEvent(any(JoinRequestAcceptedEvent.class))
        );
    }
}
