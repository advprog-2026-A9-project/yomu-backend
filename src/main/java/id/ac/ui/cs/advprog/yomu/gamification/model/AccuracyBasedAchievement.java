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
public class AccuracyBasedAchievement extends Achievement {

    @Column(name = "accuracy_threshold")
    private Integer accuracyThreshold;
}
