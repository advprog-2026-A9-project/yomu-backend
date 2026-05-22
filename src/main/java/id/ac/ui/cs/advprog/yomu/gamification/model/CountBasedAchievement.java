package id.ac.ui.cs.advprog.yomu.gamification.model;

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
public class CountBasedAchievement extends Achievement {
}
