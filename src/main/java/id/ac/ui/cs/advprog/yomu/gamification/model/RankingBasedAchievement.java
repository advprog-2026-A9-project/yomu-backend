package id.ac.ui.cs.advprog.yomu.gamification.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("RANKING_BASED")
@Getter
@Setter
@NoArgsConstructor
public class RankingBasedAchievement extends Achievement {

    @Column(name = "target_tier")
    private String targetTier;
}
