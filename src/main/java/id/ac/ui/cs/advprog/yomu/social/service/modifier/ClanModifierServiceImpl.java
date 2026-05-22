package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanModifierDTO;
import id.ac.ui.cs.advprog.yomu.social.dto.ModifierSummary;
import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanModifierRepository;
import id.ac.ui.cs.advprog.yomu.social.mapper.SocialMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanModifierServiceImpl implements ClanModifierService {

    private final ClanModifierRepository modifierRepository;
    private final ModifierEvaluatorRegistryPort evaluatorRegistry;
    private final SocialMapper socialMapper;

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

    @Override
    @Transactional(readOnly = true)
    public ModifierSummary getModifierSummary(String clanId) {
        Instant now = Instant.now();
        List<ClanModifier> all = modifierRepository.findByClanIdAndActiveTrue(clanId).stream()
                .filter(m -> isCurrentlyValid(m, now))
                .toList();

        List<ClanModifierDTO> buffs = all.stream()
                .filter(ClanModifier::isBuff)
                .map(socialMapper::toClanModifierDTO)
                .toList();

        List<ClanModifierDTO> debuffs = all.stream()
                .filter(ClanModifier::isDebuff)
                .map(socialMapper::toClanModifierDTO)
                .toList();

        double multiplier = all.stream()
                .mapToDouble(ClanModifier::getMultiplier)
                .reduce(1.0, (a, b) -> a * b);

        return new ModifierSummary(buffs, debuffs, clampMultiplier(multiplier));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, ModifierSummary> getModifierSummaries(List<String> clanIds) {
        if (clanIds == null || clanIds.isEmpty()) {
            return new HashMap<>();
        }

        Instant now = Instant.now();
        List<ClanModifier> allModifiers = modifierRepository.findByClanIdInAndActiveTrue(clanIds);

        Map<String, List<ClanModifier>> grouped = allModifiers.stream()
                .filter(m -> isCurrentlyValid(m, now))
                .collect(Collectors.groupingBy(ClanModifier::getClanId));

        Map<String, ModifierSummary> result = new HashMap<>();
        for (String clanId : clanIds) {
            List<ClanModifier> clanModifiers = grouped.getOrDefault(clanId, List.of());

            List<ClanModifierDTO> buffs = clanModifiers.stream()
                    .filter(ClanModifier::isBuff)
                    .map(socialMapper::toClanModifierDTO)
                    .toList();

            List<ClanModifierDTO> debuffs = clanModifiers.stream()
                    .filter(ClanModifier::isDebuff)
                    .map(socialMapper::toClanModifierDTO)
                    .toList();

            double multiplier = clanModifiers.stream()
                    .mapToDouble(ClanModifier::getMultiplier)
                    .reduce(1.0, (a, b) -> a * b);

            result.put(clanId, new ModifierSummary(buffs, debuffs, clampMultiplier(multiplier)));
        }

        return result;
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
