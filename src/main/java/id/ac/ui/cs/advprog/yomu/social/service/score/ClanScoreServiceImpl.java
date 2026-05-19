package id.ac.ui.cs.advprog.yomu.social.service.score;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.service.modifier.ClanModifierService;
import id.ac.ui.cs.advprog.yomu.social.strategy.ScoringStrategyResolver;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanScoreServiceImpl implements ClanScoreService {

    private final ClanRepository clanRepository;
    private final ClanValidator clanValidator;
    private final ScoringStrategyResolver scoringStrategyResolver;
    private final ClanModifierService modifierService;

    @Override
    @Transactional
    public void updateClanScore(final String clanId, int basePoints) {
        clanValidator.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);

        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        var strategy = scoringStrategyResolver.getStrategy(clan.getTier());
        int baseIncrement = strategy.calculateScore(clan, basePoints);
        double multiplier = modifierService.getActiveMultiplier(validClanId);
        int calculatedIncrement = (int) Math.round(baseIncrement * multiplier);

        clan.setScore(clan.getScore() + calculatedIncrement);
        clanRepository.save(clan);
    }
}
