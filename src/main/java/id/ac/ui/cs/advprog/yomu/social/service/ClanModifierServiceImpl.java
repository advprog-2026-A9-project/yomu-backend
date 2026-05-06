package id.ac.ui.cs.advprog.yomu.social.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanModifierRepository;
import id.ac.ui.cs.advprog.yomu.social.service.modifier.ModifierEvaluatorRegistry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanModifierServiceImpl implements ClanModifierService {

    private final ClanModifierRepository modifierRepository;
    private final ModifierEvaluatorRegistry evaluatorRegistry;

    @Override
    @Transactional
    public void evaluateModifiers(String clanId, ClanQuizStats stats) {
        evaluatorRegistry.evaluateAll(clanId, stats);
    }

    @Override
    @Transactional
    public double getActiveMultiplier(String clanId) {
        Instant now = Instant.now();
        List<ClanModifier> modifiers = modifierRepository.findByClanIdAndActiveTrue(clanId);
        double multiplier = 1.0d;

        for (ClanModifier modifier : modifiers) {
            if (modifier.getStartAt() != null && modifier.getStartAt().isAfter(now)) {
                continue;
            }

            if (modifier.getEndAt() != null && modifier.getEndAt().isBefore(now)) {
                modifier.setActive(false);
                modifierRepository.save(modifier);
                continue;
            }

            multiplier *= modifier.getMultiplier();
        }

        return clampMultiplier(multiplier);
    }

    @Override
    @Transactional
    public void clearSeasonModifiers() {
        Instant now = Instant.now();
        List<ClanModifier> activeModifiers = modifierRepository.findAll().stream()
                .filter(ClanModifier::isActive)
                .toList();

        for (ClanModifier modifier : activeModifiers) {
            modifier.setActive(false);
            modifier.setEndAt(now);
        }

        if (!activeModifiers.isEmpty()) {
            modifierRepository.saveAll(activeModifiers);
        }
    }

    private double clampMultiplier(double multiplier) {
        if (multiplier < SocialConstants.MULTIPLIER_MIN) {
            return SocialConstants.MULTIPLIER_MIN;
        }
        if (multiplier > SocialConstants.MULTIPLIER_MAX) {
            return SocialConstants.MULTIPLIER_MAX;
        }
        return multiplier;
    }
}
