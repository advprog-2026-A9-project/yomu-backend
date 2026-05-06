package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeasonServiceImpl implements SeasonService {

    private final ClanRepository clanRepository;
    private final ClanQuizStatsService statsService;
    private final ClanModifierService modifierService;

    @Override
    @Transactional
    public void endSeason() {
        List<Clan> toSave = new ArrayList<>();

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

            Set<String> topClanIds = topClans.stream()
                    .map(Clan::getId)
                    .collect(Collectors.toSet());

            for (Clan clan : topClans) {
                clan.promote();
                toSave.add(clan);
            }

            for (Clan clan : bottomClans) {
                if (!topClanIds.contains(clan.getId())) {
                    clan.demote();
                    toSave.add(clan);
                }
            }
        }

        if (!toSave.isEmpty()) {
            clanRepository.saveAllAndFlush(toSave);
        }

        clanRepository.resetAllScores();
        statsService.resetSeasonStats();
        modifierService.clearSeasonModifiers();
    }
}
