package id.ac.ui.cs.advprog.yomu.gamification.service.mission;

import java.time.LocalDate;
import java.util.List;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;

public interface DailyMissionProgressService {
    DailyMissionProgressResponse upsertDailyMissionProgress(ProgressUpdateRequest request);
    List<DailyMissionProgressResponse> getTodayDailyMissionProgressByUsername(String username);
    List<DailyMissionProgressResponse> getTodayDailyMissionDashboard(String username);
    UserDailyMissionProgress getOrCreateMissionProgress(String username, DailyMission mission, LocalDate date);
    void saveProgress(UserDailyMissionProgress progress);
}
