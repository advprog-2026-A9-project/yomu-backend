package id.ac.ui.cs.advprog.yomu.social.service.modifier.buff;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.ModifierType;

@Component
public class DailyMissionBuffDefinition implements BuffDefinition {
    @Override
    public String getKey() {
        return SocialConstants.DAILY_MISSION_BUFF_KEY;
    }

    @Override
    public ModifierType getType() {
        return ModifierType.BUFF;
    }

    @Override
    public double getMultiplier() {
        return SocialConstants.DAILY_MISSION_BUFF_MULTIPLIER;
    }

    @Override
    public Instant calculateEndAt(Instant startAt) {
        return startAt.atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant();
    }
}
