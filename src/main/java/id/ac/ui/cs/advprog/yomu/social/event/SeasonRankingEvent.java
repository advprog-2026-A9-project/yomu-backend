package id.ac.ui.cs.advprog.yomu.social.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

/**
 * Published for each ranked clan when a season ends.
 * Carries the rank, tier, and all member usernames so ranking
 * achievements can be evaluated by the gamification module.
 */
public class SeasonRankingEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final List<String> memberUsernames;
    private final String clanName;
    private final String tier;
    private final int rank;

    public SeasonRankingEvent(Object source, List<String> memberUsernames,
                              String clanName, String tier, int rank) {
        super(source);
        this.memberUsernames = memberUsernames;
        this.clanName = clanName;
        this.tier = tier;
        this.rank = rank;
    }

    public List<String> getMemberUsernames() {
        return memberUsernames;
    }

    public String getClanName() {
        return clanName;
    }

    public String getTier() {
        return tier;
    }

    public int getRank() {
        return rank;
    }
}
