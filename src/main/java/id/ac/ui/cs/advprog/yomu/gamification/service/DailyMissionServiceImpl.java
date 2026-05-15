package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.mapper.GamificationMapper;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;
import lombok.RequiredArgsConstructor;

/**
 * Daily mission service implementation
 * Dependency Inversion: depends on GamificationValidator and GamificationMapper interfaces
 */
@Service
@RequiredArgsConstructor
public class DailyMissionServiceImpl implements DailyMissionService {

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
                "DUPLICATE_NAME"
            );
        });

        DailyMission mission = new DailyMission();
        mission.setName(request.getName().trim());
        mission.setMilestone(request.getMilestone().trim());
        mission.setMissionType(request.getMissionType().trim());
        mission.setTargetCount(request.getTargetCount());
        mission.setRewardDescription(request.getRewardDescription().trim());
        mission.setActiveFrom(request.getActiveFrom() != null ? request.getActiveFrom() : LocalDate.now());
        mission.setActiveUntil(request.getActiveUntil() != null ? request.getActiveUntil() : mission.getActiveFrom().plusDays(1));

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
                "NOT_FOUND"
            ));

        dailyMissionRepository.findByNameIgnoreCase(request.getName())
            .filter(existing -> !existing.getId().equals(safeMissionId))
            .ifPresent(existing -> {
                throw new GamificationException(
                    "Daily mission with this name already exists",
                    "DUPLICATE_NAME"
                );
            });

        mission.setName(request.getName().trim());
        mission.setMilestone(request.getMilestone().trim());
        mission.setMissionType(request.getMissionType().trim());
        mission.setTargetCount(request.getTargetCount());
        mission.setRewardDescription(request.getRewardDescription().trim());
        mission.setActiveFrom(request.getActiveFrom() != null ? request.getActiveFrom() : mission.getActiveFrom());
        mission.setActiveUntil(request.getActiveUntil() != null ? request.getActiveUntil() : mission.getActiveUntil());

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
                "NOT_FOUND"
            ));

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
