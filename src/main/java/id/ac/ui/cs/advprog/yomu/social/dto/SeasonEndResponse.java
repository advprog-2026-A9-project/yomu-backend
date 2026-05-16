package id.ac.ui.cs.advprog.yomu.social.dto;

import java.util.List;

public record SeasonEndResponse(
    int processedSeasonNumber,
    int newSeasonNumber,
    List<SeasonClanSummary> promotedClans,
    List<SeasonClanSummary> relegatedClans,
    List<SeasonClanSummary> unchangedClans,
    List<SeasonTierSummary> tierSummaries
) {}