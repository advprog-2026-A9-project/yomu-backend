package id.ac.ui.cs.advprog.yomu.auth.event;

import org.springframework.context.ApplicationEvent;

public class UserCreatedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String username;
    private final String displayName;

    public UserCreatedEvent(Object source, String userId, String username, String displayName) {
        super(source);
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }
}