package id.ac.ui.cs.advprog.yomu.social.event;

import org.springframework.context.ApplicationEvent;

public class ClanCreatedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String clanId;
    private final String clanName;
    private final String tier;

    public ClanCreatedEvent(Object source, String username, String clanId, String clanName, String tier) {
        super(source);
        this.username = username;
        this.clanId = clanId;
        this.clanName = clanName;
        this.tier = tier;
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

    public String getTier() {
        return tier;
    }
}
