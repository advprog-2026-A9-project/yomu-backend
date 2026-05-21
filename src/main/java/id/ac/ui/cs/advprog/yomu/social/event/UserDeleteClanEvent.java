package id.ac.ui.cs.advprog.yomu.social.event;

import org.springframework.context.ApplicationEvent;

public class UserDeleteClanEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String clanId;

    public UserDeleteClanEvent(Object source, String clanId) {
        super(source);
        this.clanId = clanId;
    }

    public String getClanId() {
        return clanId;
    }
}
