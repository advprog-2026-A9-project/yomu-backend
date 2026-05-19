package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.util.Optional;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;

public interface ModifierEvaluator {
    String getKey();
    Optional<String> evaluate(String clanId, ClanQuizStats stats);
}