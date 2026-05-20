package id.ac.ui.cs.advprog.yomu.social.model;

import java.time.LocalDateTime;

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
@Table(name = "clan_join_requests")
@Getter
@Setter
public class ClanJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clan_id", nullable = false)
    private String clanId;


    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ClanJoinRequestStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public void accept() {
        if (status != ClanJoinRequestStatus.PENDING) {
            throw new IllegalStateException("Hanya request pending yang dapat disetujui.");
        }
        this.status = ClanJoinRequestStatus.ACCEPTED;
    }

    public void reject() {
        if (status != ClanJoinRequestStatus.PENDING) {
            throw new IllegalStateException("Hanya request pending yang dapat ditolak.");
        }
        this.status = ClanJoinRequestStatus.REJECTED;
    }
}
