package id.ac.ui.cs.advprog.yomu.social.event;

import org.springframework.context.ApplicationEvent;

public class UserJoinClanEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String clanId;
    private final String clanName;

    public UserJoinClanEvent(Object source, String userId, String clanId, String clanName) {
        super(source);
        this.userId = userId;
        this.clanId = clanId;
        this.clanName = clanName;
    }

    public String getUserId() {
        return userId;
    }

    public String getClanId() {
        return clanId;
    }

    public String getClanName() {
        return clanName;
    }
}
