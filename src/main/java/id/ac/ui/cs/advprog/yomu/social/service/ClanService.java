package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.List;
import java.util.Optional;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanJoinRequestResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;

import org.springframework.data.domain.Page;

public interface ClanService {
    Clan createClan(ClanRequest request);
    void requestJoin(String clanId, String userId, String username);
    Page<ClanJoinRequestResponse> getJoinRequests(String clanId, String leaderId, int page, int size);

    void acceptJoinRequest(String clanId, Long requestId, String leaderId);
    void rejectJoinRequest(String clanId, Long requestId, String leaderId);
    void rejectAllJoinRequests(String clanId, String leaderId);
    void joinClan(String clanId, String userId, String username, String role);
    void leaveClan(String clanId, String userId);
    Clan editClan(final String clanId, final String userId, final ClanRequest request);
    List<ClanSummaryResponse> findAll(String search);
    List<ClanSummaryResponse> findRandomClans(int limit);
    ClanDetailResponse getClanDetail(String clanId);
    Optional<MyClanResponse> getMyClanByUserId(String userId);
    void deleteClan(String clanId, String leaderId);
    List<ClanMember> getMembersByClanId(String clanId);
    
    // Leaderboard methods
    List<LeaderboardResponse> getLeaderboardByTier(String userId, String search);
    void updateClanScore(String clanId, int basePoints);
    void kickMember(String clanId, String leaderId, String memberId);
}