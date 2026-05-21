package id.ac.ui.cs.advprog.yomu.social.service.modifier.buff;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.repository.IBuffModifierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuffApplicationServiceImpl implements BuffApplicationService {

    private final IBuffModifierRepository modifierRepository;
    private final BuffDefinitionRegistry buffDefinitionRegistry;

    @Override
    @Transactional
    public void applyBuff(String clanId, String buffKey) {
        BuffDefinition definition = buffDefinitionRegistry.findByKey(buffKey).orElse(null);
        if (definition == null) {
            log.warn("No BuffDefinition found for key: {}", buffKey);
            return;
        }

        var existing = modifierRepository.findByClanIdAndKey(clanId, buffKey);
        Instant now = Instant.now();
        if (existing.isPresent() && existing.get().isActive()) {
            ClanModifier mod = existing.get();
            boolean isExpired = mod.getEndAt() != null && mod.getEndAt().isBefore(now);
            if (!isExpired) {
                log.info("Buff {} already active and not expired for clan {}. Skipping.", buffKey, clanId);
                return;
            }
        }

        ClanModifier modifier = existing
                .map(m -> {
                    m.setActive(true);
                    m.setStartAt(now);
                    m.setEndAt(definition.calculateEndAt(now));
                    return m;
                })
                .orElseGet(() -> definition.createModifier(clanId));

        modifierRepository.saveModifier(modifier);
        log.info("Applied buff {} for clan {}", buffKey, clanId);
    }

    @Override
    @Transactional
    public void deactivateBuff(String clanId, String buffKey) {
        modifierRepository.findByClanIdAndKey(clanId, buffKey)
                .ifPresentOrElse(
                        modifier -> {
                            if (!modifier.isActive()) {
                                log.info("Buff {} for clan {} already inactive. Skipping.", buffKey, clanId);
                                return;
                            }
                            modifier.setActive(false);
                            modifier.setEndAt(Instant.now());
                            modifierRepository.saveModifier(modifier);
                            log.info("Deactivated buff {} for clan {}", buffKey, clanId);
                        },
                        () -> log.warn("Buff {} not found for clan {} during deactivation.", buffKey, clanId));
    }
}
