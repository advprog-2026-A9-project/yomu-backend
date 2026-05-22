package id.ac.ui.cs.advprog.yomu.discussion.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "discussion_users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionUser {
    @Id
    private String id;
    private String username;
}