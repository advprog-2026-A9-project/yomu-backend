package id.ac.ui.cs.advprog.yomu.gamification.service.mission;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.event.DailyMissionCompletedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.mapper.GamificationMapper;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserDailyMissionProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DailyMissionProgressServiceImpl implements DailyMissionProgressService {

    private final DailyMissionRepository dailyMissionRepository;
    private final UserDailyMissionProgressRepository userDailyMissionProgressRepository;
    private final GamificationValidator validator;
    private final GamificationMapper mapper;
    private final DailyMissionRotationService dailyMissionRotationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UserDailyMissionProgress getOrCreateMissionProgress(String username, DailyMission mission, LocalDate date) {
        return userDailyMissionProgressRepository
                .findByUsernameAndDailyMissionAndProgressDate(username, mission, date)
                .orElseGet(() -> {
                    UserDailyMissionProgress p = new UserDailyMissionProgress();
                    p.setUsername(username);
                    p.setDailyMission(mission);
                    p.setProgressDate(date);
                    p.setProgressValue(0);
                    p.setCompleted(false);
                    return p;
                });
    }

    @Override
    @Transactional
    public DailyMissionProgressResponse upsertDailyMissionProgress(ProgressUpdateRequest request) {
        validator.validateMasterId(request.getMasterId());
        validator.validateUsername(request.getUsername());

        String safeMasterId = Objects.requireNonNull(request.getMasterId());
        String safeUsername = Objects.requireNonNull(request.getUsername());

        DailyMission mission = dailyMissionRepository.findById(safeMasterId)
                .orElseThrow(() -> new GamificationException(
                        "Daily mission not found",
                        "NOT_FOUND"));

        LocalDate today = LocalDate.now();

        UserDailyMissionProgress progress = getOrCreateMissionProgress(safeUsername, mission, today);

        boolean wasCompleted = progress.isCompleted();
        progress.setProgressValue(request.getProgressValue());

        int targetCountVal = mission.getTargetValue();

        if (!progress.isCompleted() && request.getProgressValue() >= targetCountVal) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            if (!wasCompleted) {
                eventPublisher.publishEvent(new DailyMissionCompletedEvent(
                        safeUsername, mission.getRewardScore()));
            }
        }

        UserDailyMissionProgress saved = userDailyMissionProgressRepository.save(progress);
        return mapper.toDailyMissionProgressResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyMissionProgressResponse> getTodayDailyMissionProgressByUsername(String username) {
        validator.validateUsername(username);

        LocalDate today = LocalDate.now();
        return userDailyMissionProgressRepository.findByUsernameAndProgressDate(username, today).stream()
                .map(mapper::toDailyMissionProgressResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<DailyMissionProgressResponse> getTodayDailyMissionDashboard(String username) {
        validator.validateUsername(username);
        LocalDate today = LocalDate.now();

        dailyMissionRotationService.ensureMissionsRotated(today);

        List<DailyMission> activeMissions = dailyMissionRepository
                .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today);

        return activeMissions.stream().map(mission -> {
            UserDailyMissionProgress progress = getOrCreateMissionProgress(username, mission, today);
            return mapper.toDailyMissionProgressResponse(progress);
        }).toList();
    }

    @Override
    @Transactional
    public void saveProgress(UserDailyMissionProgress progress) {
        userDailyMissionProgressRepository.save(progress);
    }
}
