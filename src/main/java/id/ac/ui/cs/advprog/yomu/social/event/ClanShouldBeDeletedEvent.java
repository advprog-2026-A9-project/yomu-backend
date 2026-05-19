package id.ac.ui.cs.advprog.yomu.social.event;

import org.springframework.context.ApplicationEvent;

public class ClanShouldBeDeletedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String clanId;
    private final String leaderUsername;

    public ClanShouldBeDeletedEvent(Object source, String clanId, String leaderUsername) {
        super(source);
        this.clanId = clanId;
        this.leaderUsername = leaderUsername;
    }

    public String getClanId() {
        return clanId;
    }

    public String getLeaderUsername() {
        return leaderUsername;
    }
}
