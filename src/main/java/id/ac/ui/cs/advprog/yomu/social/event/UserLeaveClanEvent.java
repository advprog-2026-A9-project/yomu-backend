package id.ac.ui.cs.advprog.yomu.social.event;

import org.springframework.context.ApplicationEvent;

public class UserLeaveClanEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String clanId;

    public UserLeaveClanEvent(Object source, String username, String clanId) {
        super(source);
        this.username = username;
        this.clanId = clanId;
    }

    public String getUsername() {
        return username;
    }

    public String getClanId() {
        return clanId;
    }
}
