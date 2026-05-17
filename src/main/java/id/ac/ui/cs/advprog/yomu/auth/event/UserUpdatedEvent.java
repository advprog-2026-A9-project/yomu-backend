package id.ac.ui.cs.advprog.yomu.auth.event;

import org.springframework.context.ApplicationEvent;

public class UserUpdatedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String userId;

    public UserUpdatedEvent(Object source, String userId) {
        super(source);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}