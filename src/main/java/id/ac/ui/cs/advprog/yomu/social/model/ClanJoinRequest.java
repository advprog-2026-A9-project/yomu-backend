package id.ac.ui.cs.advprog.yomu.social.model;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private String status; // PENDING, ACCEPTED, REJECTED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public void accept() {
        if (!SocialConstants.REQUEST_STATUS_PENDING.equals(this.status)) {
            throw new IllegalStateException("Hanya request pending yang dapat disetujui.");
        }
        this.status = SocialConstants.REQUEST_STATUS_ACCEPTED;
    }

    public void reject() {
        if (!SocialConstants.REQUEST_STATUS_PENDING.equals(this.status)) {
            throw new IllegalStateException("Hanya request pending yang dapat ditolak.");
        }
        this.status = SocialConstants.REQUEST_STATUS_REJECTED;
    }
}
