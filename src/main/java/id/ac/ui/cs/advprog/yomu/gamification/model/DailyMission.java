package id.ac.ui.cs.advprog.yomu.gamification.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_missions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "mission_category")
@Getter
@Setter
@NoArgsConstructor
public abstract class DailyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String milestone;

    @Column(name = "mission_type", nullable = false)
    private String missionType;

    @Positive(message = "Reward score must be a strictly positive integer")
    @Column(name = "reward_score", nullable = false)
    private int rewardScore;

    @Column(name = "active_from")
    private LocalDate activeFrom;

    @Column(name = "active_until")
    private LocalDate activeUntil;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public abstract int getTargetValue();

    public abstract boolean isEligibleForUpdate(int score);

    public abstract int calculateNewProgressValue(int currentProgress);

    public enum EventType {
        QUIZ_COMPLETED,
        READING_COMPLETED
    }

    public boolean isEligibleForEvent(EventType eventType) {
        DailyMissionType type = DailyMissionType.from(missionType);
        if (eventType == EventType.QUIZ_COMPLETED) {
            return type != DailyMissionType.READ_N_ARTICLES;
        } else if (eventType == EventType.READING_COMPLETED) {
            return type == DailyMissionType.READ_N_ARTICLES;
        }
        return false;
    }
}
