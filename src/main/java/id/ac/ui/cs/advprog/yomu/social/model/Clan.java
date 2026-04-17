package id.ac.ui.cs.advprog.yomu.social.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "clans")
@Getter @Setter
public class Clan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String leaderUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier tier = Tier.BRONZE;

    @Column(nullable = false)
    private int score = 0;
}