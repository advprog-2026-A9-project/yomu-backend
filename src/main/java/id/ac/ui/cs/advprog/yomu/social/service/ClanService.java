package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.List;
import java.util.Optional;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;

public interface ClanService {
    Clan createClan(ClanRequest request);
    void joinClan(String clanId, String userId, String username, String role);
    void leaveClan(String clanId, String userId);
    Clan editClan(final String clanId, final String userId, final ClanRequest request);
    List<Clan> findAll();
    Optional<MyClanResponse> getMyClanByUserId(String userId);
    void deleteClan(String clanId, String leaderId);
    List<ClanMember> getMembersByClanId(String clanId);
    
    // Leaderboard methods
    List<LeaderboardResponse> getLeaderboardByTier();
    void updateClanScore(String clanId, int basePoints);
}