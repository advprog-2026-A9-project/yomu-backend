package id.ac.ui.cs.advprog.yomu.social.model;

import java.time.Instant;

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
@Table(name = "clan_modifiers")
@Getter
@Setter
public class ClanModifier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clanId;

    @Column(nullable = false)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModifierType type;

    @Column(nullable = false)
    private double multiplier;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant startAt;

    private Instant endAt;
}
