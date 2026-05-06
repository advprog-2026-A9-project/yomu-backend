package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.model.ModifierType;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanModifierRepository;
import id.ac.ui.cs.advprog.yomu.social.service.ClanQuizStatsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccuracyPenaltyEvaluator implements ModifierEvaluator {

    private final ClanModifierRepository modifierRepository;
    private final ClanQuizStatsService statsService;

    @Override
    public String getKey() {
        return SocialConstants.LOW_ACCURACY_PENALTY_KEY;
    }

    @Override
    public void evaluate(String clanId, ClanQuizStats stats) {
        double accuracyRatio = statsService.getAccuracyRatio(stats);
        boolean shouldApply = accuracyRatio < SocialConstants.LOW_ACCURACY_THRESHOLD;

        Optional<ClanModifier> existing = modifierRepository.findByClanIdAndKey(
                clanId, getKey());

        if (shouldApply) {
            applyModifier(clanId, existing);
        } else if (existing.isPresent() && existing.get().isActive()) {
            deactivateModifier(existing.get());
        }
    }

    private void applyModifier(String clanId, Optional<ClanModifier> existing) {
        boolean needsStartReset = existing.isEmpty()
                || !existing.get().isActive()
                || existing.get().getEndAt() != null;

        ClanModifier modifier = existing.orElseGet(ClanModifier::new);
        modifier.setClanId(clanId);
        modifier.setKey(getKey());
        modifier.setType(ModifierType.DEBUFF);
        modifier.setMultiplier(SocialConstants.LOW_ACCURACY_MULTIPLIER);
        modifier.setActive(true);

        if (modifier.getStartAt() == null || needsStartReset) {
            modifier.setStartAt(Instant.now());
        }
        modifier.setEndAt(null);

        modifierRepository.save(modifier);
    }

    private void deactivateModifier(ClanModifier modifier) {
        modifier.setActive(false);
        modifier.setEndAt(Instant.now());
        modifierRepository.save(modifier);
    }

    @Override
    public ClanModifier createModifier(String clanId, boolean apply) {
        ClanModifier modifier = new ClanModifier();
        modifier.setClanId(clanId);
        modifier.setKey(getKey());
        modifier.setType(ModifierType.DEBUFF);
        modifier.setMultiplier(SocialConstants.LOW_ACCURACY_MULTIPLIER);
        modifier.setActive(apply);
        modifier.setStartAt(Instant.now());

        if (!apply) {
            modifier.setEndAt(Instant.now());
        }

        return modifier;
    }
}