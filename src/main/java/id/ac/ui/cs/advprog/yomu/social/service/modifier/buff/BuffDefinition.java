package id.ac.ui.cs.advprog.yomu.social.service.modifier.buff;

import java.time.Instant;

import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.ModifierType;

public interface BuffDefinition {
    String getKey();
    ModifierType getType();
    double getMultiplier();

    default Instant calculateEndAt(Instant startAt) {
        return null;
    }

    default ClanModifier createModifier(String clanId) {
        ClanModifier m = new ClanModifier();
        m.setClanId(clanId);
        m.setKey(getKey());
        m.setType(getType());
        m.setMultiplier(getMultiplier());
        m.setActive(true);
        Instant now = Instant.now();
        m.setStartAt(now);
        m.setEndAt(calculateEndAt(now));
        return m;
    }
}
