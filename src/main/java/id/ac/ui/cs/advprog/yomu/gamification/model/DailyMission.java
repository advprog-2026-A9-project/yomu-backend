package id.ac.ui.cs.advprog.yomu.gamification.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_missions")
@Getter
@Setter
@NoArgsConstructor
public class DailyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String milestone;

    @Column(name = "mission_type", nullable = false)
    private String missionType;

    @Column(name = "target_count", nullable = false)
    private int targetCount;

    @Column(name = "reward_description", nullable = false)
    private String rewardDescription;

    @Column(name = "active_from")
    private java.time.LocalDate activeFrom;

    @Column(name = "active_until")
    private java.time.LocalDate activeUntil;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
