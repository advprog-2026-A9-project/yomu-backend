package id.ac.ui.cs.advprog.yomu.social.service.modifier.buff;

import org.springframework.stereotype.Component;

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
}
