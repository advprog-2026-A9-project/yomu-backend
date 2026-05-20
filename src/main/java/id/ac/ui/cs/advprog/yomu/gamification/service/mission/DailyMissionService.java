package id.ac.ui.cs.advprog.yomu.gamification.service.mission;

import java.util.List;

import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;

public interface DailyMissionService {
    DailyMissionResponse create(DailyMissionRequest request);

    DailyMissionResponse update(String missionId, DailyMissionRequest request);

    void delete(String missionId);

    List<DailyMissionResponse> findAll();
}
