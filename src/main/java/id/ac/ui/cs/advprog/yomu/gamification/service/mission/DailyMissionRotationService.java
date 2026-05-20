package id.ac.ui.cs.advprog.yomu.gamification.service.mission;

import java.time.LocalDate;
import java.util.List;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;

public interface DailyMissionRotationService {
    void ensureMissionsRotated(LocalDate today);
    void rotateMissions();
    void forceRotateMissions();
    void setTodayMissions(List<String> missionIds);
    List<DailyMission> getActiveDailyMissions(LocalDate date);
}
