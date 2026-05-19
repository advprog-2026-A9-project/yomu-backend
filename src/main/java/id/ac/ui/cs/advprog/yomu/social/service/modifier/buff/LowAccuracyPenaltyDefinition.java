package id.ac.ui.cs.advprog.yomu.social.service.modifier.buff;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.ModifierType;

@Component
public class LowAccuracyPenaltyDefinition implements BuffDefinition {
    @Override
    public String getKey() {
        return SocialConstants.LOW_ACCURACY_PENALTY_KEY;
    }

    @Override
    public ModifierType getType() {
        return ModifierType.DEBUFF;
    }

    @Override
    public double getMultiplier() {
        return SocialConstants.LOW_ACCURACY_MULTIPLIER;
    }

    @Override
    public java.time.Instant calculateEndAt(java.time.Instant startAt) {
        return startAt.plus(java.time.Duration.ofHours(3));
    }
}
