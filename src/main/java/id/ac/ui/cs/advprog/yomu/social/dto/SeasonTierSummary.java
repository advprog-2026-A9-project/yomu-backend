package id.ac.ui.cs.advprog.yomu.social.dto;

import java.util.List;

public record SeasonTierSummary(
    String tier,
    List<SeasonClanSummary> topClans
) {}