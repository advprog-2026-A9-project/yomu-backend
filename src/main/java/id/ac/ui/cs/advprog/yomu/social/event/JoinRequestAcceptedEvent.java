package id.ac.ui.cs.advprog.yomu.social.event;

import org.springframework.context.ApplicationEvent;

public class JoinRequestAcceptedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String clanId;
    private final String clanName;

    public JoinRequestAcceptedEvent(Object source, String username, String clanId, String clanName) {
        super(source);
        this.username = username;
        this.clanId = clanId;
        this.clanName = clanName;
    }

    public String getUsername() {
        return username;
    }

    public String getClanId() {
        return clanId;
    }

    public String getClanName() {
        return clanName;
    }
}
