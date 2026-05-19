package id.ac.ui.cs.advprog.yomu.social.port;

import java.util.Optional;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;

public interface ClanLookupPort {
    Optional<Clan> findClanById(String clanId);
}
