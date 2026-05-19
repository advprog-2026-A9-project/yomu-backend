package id.ac.ui.cs.advprog.yomu.gamification.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("ACCURACY_BASED")
@Getter
@Setter
@NoArgsConstructor
public class AccuracyDailyMission extends DailyMission {

    @Column(name = "accuracy_threshold")
    private int accuracyThreshold;

    @Column(name = "required_count")
    private int requiredCount;

    @Override
    public int getTargetValue() {
        return accuracyThreshold;
    }

    @Override
    public boolean isEligibleForUpdate(int score) {
        return score >= accuracyThreshold;
    }

    @Override
    public int calculateNewProgressValue(int currentProgress) {
        int step = requiredCount > 0 ? accuracyThreshold / requiredCount : 0;
        int currentCompleted = step > 0 ? currentProgress / step : 0;
        int nextCompleted = currentCompleted + 1;
        if (nextCompleted >= requiredCount) {
            return accuracyThreshold;
        } else {
            return nextCompleted * step;
        }
    }
}
