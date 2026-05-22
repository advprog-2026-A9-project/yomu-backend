package id.ac.ui.cs.advprog.yomu.discussion.repository;

import id.ac.ui.cs.advprog.yomu.discussion.model.DiscussionUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussionUserRepository extends JpaRepository<DiscussionUser, String> {
}