package id.ac.ui.cs.advprog.yomu.gamification.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("COUNT_BASED")
@Getter
@Setter
@NoArgsConstructor
public class CountBasedDailyMission extends DailyMission {

    @Column(name = "target_count")
    private int targetCount;

    @Override
    public int getTargetValue() {
        return targetCount;
    }

    @Override
    public boolean isEligibleForUpdate(int score) {
        return true;
    }

    @Override
    public int calculateNewProgressValue(int currentProgress) {
        return currentProgress + 1;
    }
}
