package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanModifierRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanModifierServiceImpl implements ClanModifierService {

    private final ClanModifierRepository modifierRepository;
    private final ModifierEvaluatorRegistryPort evaluatorRegistry;

    @Override
    @Transactional
    public void evaluateModifiers(String clanId, ClanQuizStats stats) {
        evaluatorRegistry.evaluateAll(clanId, stats);
    }

    @Override
    @Transactional(readOnly = true)
    public double getActiveMultiplier(String clanId) {
        Instant now = Instant.now();
        double multiplier = modifierRepository.findByClanIdAndActiveTrue(clanId).stream()
                .filter(m -> isCurrentlyValid(m, now))
                .mapToDouble(ClanModifier::getMultiplier)
                .reduce(1.0, (a, b) -> a * b);

        return clampMultiplier(multiplier);
    }

    private boolean isCurrentlyValid(ClanModifier modifier, Instant now) {
        if (modifier.getStartAt() != null && modifier.getStartAt().isAfter(now)) {
            return false;
        }
        if (modifier.getEndAt() != null && modifier.getEndAt().isBefore(now)) {
            return false;
        }
        return true;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireStaleModifiers() {
        modifierRepository.deactivateExpired(Instant.now());
    }

    private double clampMultiplier(double multiplier) {
        if (multiplier < SocialConstants.MULTIPLIER_MIN) return SocialConstants.MULTIPLIER_MIN;
        if (multiplier > SocialConstants.MULTIPLIER_MAX) return SocialConstants.MULTIPLIER_MAX;
        return multiplier;
    }
}
