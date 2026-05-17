package id.ac.ui.cs.advprog.yomu.social.event;

import org.springframework.context.ApplicationEvent;

public class ClanNameChangedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String clanId;
    private final String newClanName;

    public ClanNameChangedEvent(Object source, String clanId, String newClanName) {
        super(source);
        this.clanId = clanId;
        this.newClanName = newClanName;
    }

    public String getClanId() {
        return clanId;
    }

    public String getNewClanName() {
        return newClanName;
    }
}
