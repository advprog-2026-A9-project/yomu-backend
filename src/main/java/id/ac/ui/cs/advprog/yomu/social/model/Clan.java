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

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private String leaderUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier tier = Tier.BRONZE;

    @Column(nullable = false)
    private int score = 0;

    public void promote() {
        if (this.tier != Tier.DIAMOND) {
            this.tier = this.tier.promote();
        }
        this.score = 0;
    }

    public void demote() {
        if (this.tier != Tier.BRONZE) {
            this.tier = this.tier.demote();
        }
        this.score = 0;
    }
}