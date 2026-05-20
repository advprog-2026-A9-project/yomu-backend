package id.ac.ui.cs.advprog.yomu.gamification.service.achievement;

import java.util.List;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ShowcaseUpdateRequest;

public interface AchievementShowcaseService {
    List<String> getShowcaseByUsername(String username);
    void updateShowcase(ShowcaseUpdateRequest request);
}
