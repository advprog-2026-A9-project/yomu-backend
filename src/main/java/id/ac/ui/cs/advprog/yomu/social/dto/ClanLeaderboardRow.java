package id.ac.ui.cs.advprog.yomu.social.dto;

import id.ac.ui.cs.advprog.yomu.social.model.Tier;

public interface ClanLeaderboardRow {
    String getClanId();

    String getClanName();

    Tier getTier();

    int getScore();

    long getMemberCount();
}