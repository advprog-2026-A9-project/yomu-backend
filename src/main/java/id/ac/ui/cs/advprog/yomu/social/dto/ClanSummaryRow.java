package id.ac.ui.cs.advprog.yomu.social.dto;

import id.ac.ui.cs.advprog.yomu.social.model.Tier;

public interface ClanSummaryRow {
    String getClanId();
    String getClanName();
    String getDescription();
    String getLeaderUsername();
    Tier getTier();
    int getScore();
    long getMemberCount();
}
