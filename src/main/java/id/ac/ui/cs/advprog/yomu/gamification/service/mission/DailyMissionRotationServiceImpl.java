package id.ac.ui.cs.advprog.yomu.gamification.service.mission;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DailyMissionRotationServiceImpl implements DailyMissionRotationService {

    private static final int DAILY_MISSION_SLOT = 3;

    private final DailyMissionRepository dailyMissionRepository;
    private final GamificationValidator validator;

    @Override
    @Transactional
    public void ensureMissionsRotated(LocalDate today) {
        List<DailyMission> existing = dailyMissionRepository
                .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today);

        if (!existing.isEmpty()) {
            return;
        }

        rotateMissions();
    }

    @Override
    @Transactional
    public void rotateMissions() {
        LocalDate today = LocalDate.now();
        List<DailyMission> existing = dailyMissionRepository
                .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today);

        if (!existing.isEmpty()) {
            return;
        }

        List<DailyMission> pool = dailyMissionRepository.findAll().stream()
                .filter(DailyMission::isActive)
                .toList();

        if (pool.isEmpty()) {
            return;
        }

        List<DailyMission> mutablePool = new ArrayList<>(pool);
        Collections.shuffle(mutablePool);
        List<DailyMission> selected = mutablePool.stream().limit(DAILY_MISSION_SLOT).toList();

        for (DailyMission mission : selected) {
            mission.setActiveFrom(today);
            mission.setActiveUntil(today);
            dailyMissionRepository.save(mission);
        }
    }

    private void expireExistingMissions(LocalDate today) {
        dailyMissionRepository
                .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today)
                .forEach(m -> {
                    m.setActiveUntil(today.minusDays(1));
                    dailyMissionRepository.save(m);
                });
    }

    @Override
    @Transactional
    public void forceRotateMissions() {
        LocalDate today = LocalDate.now();
        expireExistingMissions(today);
        rotateMissions();
    }

    @Override
    @Transactional
    public void setTodayMissions(List<String> missionIds) {
        if (missionIds == null || missionIds.size() != DAILY_MISSION_SLOT) {
            throw new GamificationException("Exactly " + DAILY_MISSION_SLOT + " daily missions must be selected", "INVALID_REQUEST");
        }

        LocalDate today = LocalDate.now();
        expireExistingMissions(today);

        for (String id : missionIds) {
            validator.validateMasterId(id);
            DailyMission mission = dailyMissionRepository.findById(Objects.requireNonNull(id))
                    .orElseThrow(() -> new GamificationException("Daily mission not found: " + id, "NOT_FOUND"));
            mission.setActiveFrom(today);
            mission.setActiveUntil(today);
            dailyMissionRepository.save(mission);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyMission> getActiveDailyMissions(LocalDate date) {
        return dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(date,
                date);
    }
}
