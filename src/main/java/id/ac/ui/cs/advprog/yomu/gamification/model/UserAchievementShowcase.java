package id.ac.ui.cs.advprog.yomu.gamification.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_achievement_showcases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievementShowcase {

    @Id
    private String userId;

    @ElementCollection
    @CollectionTable(name = "user_showcase_achievements", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "achievement_id")
    @Builder.Default
    private List<String> achievementIds = new ArrayList<>();
}
