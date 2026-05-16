package id.ac.ui.cs.advprog.yomu.social.dto;

public record SeasonClanSummary(
    String clanId,
    String clanName,
    String tier,
    int score,
    long memberCount
) {}