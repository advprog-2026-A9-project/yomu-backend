package id.ac.ui.cs.advprog.yomu.social.service.clan.lifecycle;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;

public interface ClanLifecycleService {
    Clan createClan(ClanRequest request);
    Clan editClan(String clanId, String leaderUsername, ClanRequest request);
    void deleteClan(String clanId, String leaderUsername);
}
