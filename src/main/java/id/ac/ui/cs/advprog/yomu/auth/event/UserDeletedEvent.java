package id.ac.ui.cs.advprog.yomu.auth.event;

import org.springframework.context.ApplicationEvent;

public class UserDeletedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String username;

    public UserDeletedEvent(Object source, String userId, String username) {
        super(source);
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}