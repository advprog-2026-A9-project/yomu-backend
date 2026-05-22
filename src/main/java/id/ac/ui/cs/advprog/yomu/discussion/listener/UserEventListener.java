package id.ac.ui.cs.advprog.yomu.discussion.listener;

import id.ac.ui.cs.advprog.yomu.auth.event.UserCreatedEvent;
import id.ac.ui.cs.advprog.yomu.auth.event.UserUpdatedEvent;
import id.ac.ui.cs.advprog.yomu.discussion.model.DiscussionUser;
import id.ac.ui.cs.advprog.yomu.discussion.repository.DiscussionUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final DiscussionUserRepository discussionUserRepository;

    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        DiscussionUser user = DiscussionUser.builder()
                .id(event.getUserId())
                .username(event.getUsername())
                .build();
        discussionUserRepository.save(user);
    }

    @EventListener
    public void handleUserUpdated(UserUpdatedEvent event) {
        discussionUserRepository.findById(event.getUserId())
                .ifPresent(user -> {
                    user.setUsername(event.getUsername());
                    discussionUserRepository.save(user);
                });
    }
}