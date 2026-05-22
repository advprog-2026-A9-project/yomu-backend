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
@SuppressWarnings("null")
class ScoringStrategyTest {

    private static final String CLAN_123 = "clan-123";

    @Mock
    private ClanQuizStatsRepository quizStatsRepository;

    @Mock
    private ClanMemberValidationPort memberValidationPort;

    private Clan clan;

    @BeforeEach
    void setUp() {
        clan = new Clan();
        clan.setId(CLAN_123);
        clan.setScore(1000);
        clan.setTier(Tier.BRONZE);
    }

    @Test
    void bronzeScoringStrategy_ShouldReturnBasePoints() {
        BronzeScoringStrategy strategy = new BronzeScoringStrategy();

        assertAll("Verify Bronze strategy values",
                () -> assertEquals(Tier.BRONZE, strategy.getSupportedTier(), "Supported tier should be BRONZE"),
                () -> assertEquals(10, strategy.calculateScore(clan, 10), "Calculated score should equal base points"));
    }

    @Test
    void silverScoringStrategy_WhenStatsExist_ShouldApplyBonus() {
        SilverScoringStrategy strategy = new SilverScoringStrategy(quizStatsRepository);

        ClanQuizStats stats = new ClanQuizStats();
        stats.setTotalQuizAttempts(2L); // bonusMultiplier = 1.0 + (2 * 0.05) = 1.10
        when(quizStatsRepository.findById(CLAN_123)).thenReturn(Optional.of(stats));

        int result = strategy.calculateScore(clan, 10);

        // round(10 * 1.10) = 11
        assertAll("Verify Silver strategy values with stats",
                () -> assertEquals(Tier.SILVER, strategy.getSupportedTier(), "Supported tier should be SILVER"),
                () -> assertEquals(11, result, "Score should include bonus from quiz attempts"));
    }

    @Test
    void silverScoringStrategy_WhenStatsNotExist_ShouldApplyDefaultMultiplier() {
        SilverScoringStrategy strategy = new SilverScoringStrategy(quizStatsRepository);
        when(quizStatsRepository.findById(CLAN_123)).thenReturn(Optional.empty());

        int result = strategy.calculateScore(clan, 10);

        // bonusMultiplier = 1.0 + (0 * 0.05) = 1.0 -> round(10 * 1.0) = 10
        assertEquals(10, result, "Score should apply default 1.0 multiplier when stats do not exist");
    }

    @Test
    void goldScoringStrategy_ShouldApplyDiminishingReturns() {
        GoldScoringStrategy strategy = new GoldScoringStrategy();

        // factor = 1.0 / (1.0 + ln(1 + 1000/1000)) = 1.0 / (1.0 + ln(2)) = 1.0 / 1.693 = 0.59
        // 100 * 0.59 = 59
        int result = strategy.calculateScore(clan, 100);

        assertAll("Verify Gold strategy values",
                () -> assertEquals(Tier.GOLD, strategy.getSupportedTier(), "Supported tier should be GOLD"),
                () -> assertEquals(59, result, "Score should apply logarithmic diminishing returns based on existing score"));
    }

    @Test
    void diamondScoringStrategy_ShouldApplyWeightedAverage() {
        DiamondScoringStrategy strategy = new DiamondScoringStrategy(memberValidationPort);

        when(memberValidationPort.countByClanId(CLAN_123)).thenReturn(10L);

        // averageContribution = 1000 / 10 = 100
        // weighted = (100 * 0.6) + (50 * 0.4) = 60 + 20 = 80
        int result = strategy.calculateScore(clan, 50);

        assertAll("Verify Diamond strategy values",
                () -> assertEquals(Tier.DIAMOND, strategy.getSupportedTier(), "Supported tier should be DIAMOND"),
                () -> assertEquals(80, result, "Score should apply weighted average contribution formula"));
    }

    @Test
    void diamondScoringStrategy_WhenMemberCountZero_ShouldUseOneAsDenominator() {
        DiamondScoringStrategy strategy = new DiamondScoringStrategy(memberValidationPort);
        when(memberValidationPort.countByClanId(CLAN_123)).thenReturn(0L);

        // averageContribution = 1000 / 1 = 1000
        // weighted = (1000 * 0.6) + (50 * 0.4) = 600 + 20 = 620
        int result = strategy.calculateScore(clan, 50);
        assertEquals(620, result, "Score should use 1 as denominator if member count is 0");
    }

    @Test
    void scoringStrategyFactory_ShouldResolveStrategiesCorrectly() {
        BronzeScoringStrategy bronze = new BronzeScoringStrategy();
        GoldScoringStrategy gold = new GoldScoringStrategy();
        ScoringStrategyFactory factory = new ScoringStrategyFactory(List.of(bronze, gold));

        assertAll("Verify factory strategy resolution",
                () -> assertEquals(bronze, factory.getStrategy(Tier.BRONZE), "Should resolve Bronze strategy"),
                () -> assertEquals(gold, factory.getStrategy(Tier.GOLD), "Should resolve Gold strategy"));
    }

    @Test
    void scoringStrategyFactory_WhenTierNotFound_ShouldThrowException() {
        BronzeScoringStrategy bronze = new BronzeScoringStrategy();
        ScoringStrategyFactory factory = new ScoringStrategyFactory(List.of(bronze));

        assertAll("Verify factory exception behavior",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> factory.getStrategy(Tier.GOLD),
                            "Should throw IllegalArgumentException");
                    assertEquals("No scoring strategy found for tier: GOLD", ex.getMessage(), "Message should match");
                });
    }
}
