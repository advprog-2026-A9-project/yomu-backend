package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.List;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;

public interface ClanService {
    Clan createClan(ClanRequest request);
    void joinClan(String clanId, String userId);
    void leaveClan(String clanId, String userId);
    List<Clan> findAll();
    void deleteClan(String clanId, String leaderId);
}