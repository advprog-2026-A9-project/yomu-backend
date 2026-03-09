package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.List;
import java.util.Optional;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;

public interface ClanService {
    Clan createClan(ClanRequest request);
    void joinClan(String clanId, String userId);
    void leaveClan(String clanId, String userId);
    List<Clan> findAll();
    Optional<MyClanResponse> getMyClanByUserId(String userId);
    void deleteClan(String clanId, String leaderId);
    
    // Leaderboard methods
    List<LeaderboardResponse> getLeaderboardByTier();
    void updateClanScore(String clanId, int basePoints);
    void endSeason(); // Admin: promote/demote clans
}