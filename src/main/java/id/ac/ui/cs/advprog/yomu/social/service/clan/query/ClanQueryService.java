package id.ac.ui.cs.advprog.yomu.social.service.clan.query;

import java.util.List;
import java.util.Optional;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;

public interface ClanQueryService {
    List<ClanSummaryResponse> findAll(String search);
    List<ClanSummaryResponse> findRandomClans(int limit);
    ClanDetailResponse getClanDetail(String clanId);
    Optional<MyClanResponse> getMyClanByUsername(String username);
    List<LeaderboardResponse> getLeaderboardByTier(String username, String search);
}
