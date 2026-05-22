package id.ac.ui.cs.advprog.yomu.social.strategy;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.port.ClanMemberValidationPort;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanQuizStatsRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD")
class ScoringStrategyTest {

    private static final String CLAN_ID = "clan-123";
    private static final String CLAN_NAME = "Wibu Indo";

    // Assertion Messages
    private static final String MSG_SCORE = "Calculated score should match";
    private static final String MSG_SUPPORTED_TIER = "Supported tier should match";

    @Mock
    private ClanQuizStatsRepository quizStatsRepository;

    @Mock
    private ClanMemberValidationPort memberValidationPort;

    private Clan dummyClan;

    @BeforeEach
    void setUp() {
        dummyClan = new Clan();
        dummyClan.setId(CLAN_ID);
        dummyClan.setName(CLAN_NAME);
        dummyClan.setScore(1000);
    }

    @Test
    void bronzeScoringStrategy_CalculateScore() {
        BronzeScoringStrategy strategy = new BronzeScoringStrategy();

        int score = strategy.calculateScore(dummyClan, 100);

        assertAll("Verify BronzeScoringStrategy behavior",
                () -> assertEquals(100, score, MSG_SCORE),
                () -> assertEquals(Tier.BRONZE, strategy.getSupportedTier(), MSG_SUPPORTED_TIER)
        );
    }

    @Test
    void silverScoringStrategy_CalculateScore_WithExistingStats() {
        SilverScoringStrategy strategy = new SilverScoringStrategy(quizStatsRepository);
        ClanQuizStats stats = new ClanQuizStats();
        stats.setClanId(CLAN_ID);
        stats.setTotalQuizAttempts(4); // 4 * 0.05 = 0.20 bonus (1.20 multiplier)

        when(quizStatsRepository.findById(CLAN_ID)).thenReturn(Optional.of(stats));

        int score = strategy.calculateScore(dummyClan, 100);

        assertAll("Verify SilverScoringStrategy with existing stats behavior",
                () -> assertEquals(120, score, MSG_SCORE),
                () -> assertEquals(Tier.SILVER, strategy.getSupportedTier(), MSG_SUPPORTED_TIER)
        );
    }

    @Test
    void silverScoringStrategy_CalculateScore_NoStats() {
        SilverScoringStrategy strategy = new SilverScoringStrategy(quizStatsRepository);

        when(quizStatsRepository.findById(CLAN_ID)).thenReturn(Optional.empty());

        int score = strategy.calculateScore(dummyClan, 100);

        assertEquals(100, score, MSG_SCORE);
    }

    @Test
    void goldScoringStrategy_CalculateScore() {
        GoldScoringStrategy strategy = new GoldScoringStrategy();

        // clan.getScore() = 1000.
        // factor = 1.0 / (1.0 + Math.log1p(1000.0 / 1000.0)) = 1.0 / (1.0 + Math.log1p(1.0)) = 1.0 / (1.0 + Math.log(2))
        // Math.log(2) is approx 0.693147. factor is approx 1.0 / 1.693147 = 0.5906.
        // score for 100 points = 100 * 0.5906 = 59.06 -> rounded to 59.
        int score = strategy.calculateScore(dummyClan, 100);

        assertAll("Verify GoldScoringStrategy behavior",
                () -> assertEquals(59, score, MSG_SCORE),
                () -> assertEquals(Tier.GOLD, strategy.getSupportedTier(), MSG_SUPPORTED_TIER)
        );
    }

    @Test
    void diamondScoringStrategy_CalculateScore() {
        DiamondScoringStrategy strategy = new DiamondScoringStrategy(memberValidationPort);

        when(memberValidationPort.countByClanId(CLAN_ID)).thenReturn(10L);

        // averageContribution = clan.getScore() / Math.max(10, 1) = 1000 / 10 = 100.
        // weighted = (100 * 0.6) + (200 * 0.4) = 60 + 80 = 140.
        int score = strategy.calculateScore(dummyClan, 200);

        assertAll("Verify DiamondScoringStrategy behavior",
                () -> assertEquals(140, score, MSG_SCORE),
                () -> assertEquals(Tier.DIAMOND, strategy.getSupportedTier(), MSG_SUPPORTED_TIER)
        );
    }

    @Test
    void scoringStrategyFactory_GetStrategySuccess() {
        BronzeScoringStrategy bronze = new BronzeScoringStrategy();
        GoldScoringStrategy gold = new GoldScoringStrategy();
        ScoringStrategyFactory factory = new ScoringStrategyFactory(List.of(bronze, gold));

        ScoringStrategy resolved = factory.getStrategy(Tier.BRONZE);
        assertEquals(bronze, resolved, "Should resolve Bronze strategy for BRONZE tier");
    }

    @Test
    void scoringStrategyFactory_GetStrategyNotFound_ThrowsIllegalArgumentException() {
        BronzeScoringStrategy bronze = new BronzeScoringStrategy();
        ScoringStrategyFactory factory = new ScoringStrategyFactory(List.of(bronze));

        assertThrows(IllegalArgumentException.class, () ->
            factory.getStrategy(Tier.GOLD),
            "Should throw IllegalArgumentException when no strategy matches tier"
        );
    }
}
