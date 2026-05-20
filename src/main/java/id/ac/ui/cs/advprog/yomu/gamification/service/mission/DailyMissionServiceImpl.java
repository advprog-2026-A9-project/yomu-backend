package id.ac.ui.cs.advprog.yomu.gamification.service.mission;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.mapper.GamificationMapper;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyMissionServiceImpl implements DailyMissionService {

    private static final String MISSION_TYPE_ACCURACY = "achieve_accuracy";

    private final DailyMissionRepository dailyMissionRepository;
    private final GamificationValidator validator;
    private final GamificationMapper mapper;

    @Override
    @Transactional
    public DailyMissionResponse create(DailyMissionRequest request) {
        validator.validateDailyMissionRequest(request);

        dailyMissionRepository.findByNameIgnoreCase(request.getName()).ifPresent(existing -> {
            throw new GamificationException(
                    "Daily mission with this name already exists",
                    "DUPLICATE_NAME");
        });

        DailyMission mission;
        if (MISSION_TYPE_ACCURACY.equalsIgnoreCase(request.getMissionType().trim())) {
            AccuracyDailyMission accMission = new AccuracyDailyMission();
            accMission
                    .setAccuracyThreshold(request.getAccuracyThreshold() != null ? request.getAccuracyThreshold() : 0);
            accMission.setRequiredCount(request.getRequiredCount() != null ? request.getRequiredCount() : 1);
            mission = accMission;
        } else {
            CountBasedDailyMission countMission = new CountBasedDailyMission();
            countMission.setTargetCount(request.getTargetCount() != null ? request.getTargetCount() : 1);
            mission = countMission;
        }

        mission.setName(request.getName().trim());
        mission.setMilestone(request.getMilestone().trim());
        mission.setMissionType(request.getMissionType().trim());
        mission.setRewardScore(request.getRewardScore());
        mission.setActiveFrom(request.getActiveFrom() != null ? request.getActiveFrom() : LocalDate.now());
        mission.setActiveUntil(
                request.getActiveUntil() != null ? request.getActiveUntil() : mission.getActiveFrom().plusDays(1));

        DailyMission saved = dailyMissionRepository.save(mission);
        return mapper.toDailyMissionResponse(saved);
    }

    @Override
    @Transactional
    public DailyMissionResponse update(String missionId, DailyMissionRequest request) {
        validator.validateDailyMissionRequest(request);
        validator.validateMasterId(missionId);

        String safeMissionId = Objects.requireNonNull(missionId);

        DailyMission mission = dailyMissionRepository.findById(safeMissionId)
                .orElseThrow(() -> new GamificationException(
                        "Daily mission not found",
                        "NOT_FOUND"));

        dailyMissionRepository.findByNameIgnoreCase(request.getName())
                .filter(existing -> !existing.getId().equals(safeMissionId))
                .ifPresent(existing -> {
                    throw new GamificationException(
                            "Daily mission with this name already exists",
                            "DUPLICATE_NAME");
                });

        boolean isRequestAccuracy = MISSION_TYPE_ACCURACY.equalsIgnoreCase(request.getMissionType().trim());
        boolean isExistingAccuracy = mission instanceof AccuracyDailyMission;

        if (isRequestAccuracy != isExistingAccuracy) {
            throw new GamificationException(
                    "Cannot change daily mission category",
                    "INVALID_TYPE_CHANGE");
        }

        mission.setName(request.getName().trim());
        mission.setMilestone(request.getMilestone().trim());
        mission.setMissionType(request.getMissionType().trim());
        mission.setRewardScore(request.getRewardScore());
        mission.setActiveFrom(request.getActiveFrom() != null ? request.getActiveFrom() : mission.getActiveFrom());
        mission.setActiveUntil(request.getActiveUntil() != null ? request.getActiveUntil() : mission.getActiveUntil());

        if (mission instanceof AccuracyDailyMission accuracyMission) {
            accuracyMission
                    .setAccuracyThreshold(request.getAccuracyThreshold() != null ? request.getAccuracyThreshold() : 0);
            accuracyMission.setRequiredCount(request.getRequiredCount() != null ? request.getRequiredCount() : 1);
        } else if (mission instanceof CountBasedDailyMission countMission) {
            countMission.setTargetCount(request.getTargetCount() != null ? request.getTargetCount() : 1);
        }

        DailyMission saved = dailyMissionRepository.save(mission);
        return mapper.toDailyMissionResponse(saved);
    }

    @Override
    @Transactional
    public void delete(String missionId) {
        validator.validateMasterId(missionId);

        String safeMissionId = Objects.requireNonNull(missionId);

        DailyMission mission = dailyMissionRepository.findById(safeMissionId)
                .orElseThrow(() -> new GamificationException(
                        "Daily mission not found",
                        "NOT_FOUND"));

        dailyMissionRepository.delete(Objects.requireNonNull(mission));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyMissionResponse> findAll() {
        return dailyMissionRepository.findAll()
                .stream()
                .map(mapper::toDailyMissionResponse)
                .toList();
    }
}
