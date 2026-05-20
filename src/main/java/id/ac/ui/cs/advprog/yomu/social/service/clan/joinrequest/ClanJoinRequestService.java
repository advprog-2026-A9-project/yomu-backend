package id.ac.ui.cs.advprog.yomu.social.service.clan.joinrequest;

import org.springframework.data.domain.Page;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanJoinRequestResponse;

public interface ClanJoinRequestService {
    void requestJoin(String clanId, String username);
    Page<ClanJoinRequestResponse> getJoinRequests(String clanId, String leaderId, int page, int size);
    void acceptJoinRequest(String clanId, Long requestId, String leaderId);
    void rejectJoinRequest(String clanId, Long requestId, String leaderId);
    void rejectAllJoinRequests(String clanId, String leaderId);
}
