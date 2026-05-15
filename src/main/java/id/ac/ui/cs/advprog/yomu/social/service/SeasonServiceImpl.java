package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonClanSummary;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonEndResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonStatusResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonTierSummary;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.SeasonStateRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeasonServiceImpl implements SeasonService {

    private final ClanRepository clanRepository;
    private final ClanMemberRepository memberRepository;
    private final ClanQuizStatsService statsService;
    private final ClanModifierService modifierService;
    private final SeasonStateRepository seasonStateRepository;

    @Override
    @Transactional
    public SeasonStatusResponse getCurrentSeason() {
        return seasonStateRepository.findTopByOrderByIdDesc()
            .map(state -> new SeasonStatusResponse(state.getSeasonNumber(), state.isActive() ? "Active" : "Ended"))
            .orElseGet(() -> new SeasonStatusResponse(1, "Active"));
    }

    @Override
    @Transactional
    public SeasonEndResponse endSeason() {
        int currentSeasonNumber = getCurrentSeason().seasonNumber();
        List<Clan> toSave = new ArrayList<>();
        List<SeasonClanSummary> promotedClans = new ArrayList<>();
        List<SeasonClanSummary> relegatedClans = new ArrayList<>();
        List<SeasonClanSummary> unchangedClans = new ArrayList<>();
        List<SeasonTierSummary> tierSummaries = new ArrayList<>();

        for (Tier tier : Tier.values()) {
            long totalClans = clanRepository.countByTier(tier);
            if (totalClans <= 0) {
                continue;
            }

            int changeCount = (int) Math.ceil(totalClans * SocialConstants.SEASON_CHANGE_RATIO);
            int promoteCount = Math.max(1, changeCount);
            int demoteCount = Math.max(1, changeCount);

            List<Clan> topClans = clanRepository.findTopClansByTier(
                    tier,
                    PageRequest.of(0, promoteCount));
            List<Clan> bottomClans = clanRepository.findBottomClansByTier(
                    tier,
                    PageRequest.of(0, demoteCount));

            List<SeasonClanSummary> topSummaries = topClans.stream().map(this::toSummary).toList();
            List<SeasonClanSummary> bottomSummaries = bottomClans.stream().map(this::toSummary).toList();

            tierSummaries.add(new SeasonTierSummary(
                tier.getDisplayName(),
                topSummaries
            ));

            Set<String> topClanIds = topClans.stream()
                    .map(Clan::getId)
                    .collect(Collectors.toSet());

            for (Clan clan : topClans) {
                clan.promote();
                toSave.add(clan);
            }

            promotedClans.addAll(topSummaries);

            for (Clan clan : bottomClans) {
                if (!topClanIds.contains(clan.getId())) {
                    clan.demote();
                    toSave.add(clan);
                }
            }

            relegatedClans.addAll(bottomSummaries.stream()
                .filter(summary -> topClanIds.stream().noneMatch(summary.clanId()::equals))
                .toList());

            for (Clan clan : clanRepository.findTopClansByTier(tier, PageRequest.of(0, Math.toIntExact(totalClans)))) {
                if (!topClanIds.contains(clan.getId()) && bottomClans.stream().noneMatch(bottom -> bottom.getId().equals(clan.getId()))) {
                    unchangedClans.add(toSummary(clan));
                }
            }
        }

        if (!toSave.isEmpty()) {
            clanRepository.saveAllAndFlush(toSave);
        }

        clanRepository.resetAllScores();
        statsService.resetSeasonStats();
        modifierService.clearSeasonModifiers();

        int newSeasonNumber = currentSeasonNumber + 1;
        var seasonState = seasonStateRepository.findTopByOrderByIdDesc().orElseGet(id.ac.ui.cs.advprog.yomu.social.model.SeasonState::new);
        seasonState.setSeasonNumber(newSeasonNumber);
        seasonState.setActive(true);
        seasonStateRepository.save(seasonState);

        return new SeasonEndResponse(
            currentSeasonNumber,
            newSeasonNumber,
            promotedClans,
            relegatedClans,
            unchangedClans,
            tierSummaries
        );
    }

    private SeasonClanSummary toSummary(Clan clan) {
        return new SeasonClanSummary(
            clan.getId(),
            clan.getName(),
            clan.getTier().getDisplayName(),
            clan.getScore(),
            memberRepository.countByClanId(clan.getId())
        );
    }
}
