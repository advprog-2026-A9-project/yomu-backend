package id.ac.ui.cs.advprog.yomu.social.service.score;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.service.modifier.ClanModifierService;
import id.ac.ui.cs.advprog.yomu.social.strategy.ScoringStrategy;
import id.ac.ui.cs.advprog.yomu.social.strategy.ScoringStrategyResolver;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ClanScoreServiceImplTest {

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanValidator clanValidator;

    @Mock
    private ScoringStrategyResolver scoringStrategyResolver;

    @Mock
    private ClanModifierService modifierService;

    @Mock
    private ScoringStrategy scoringStrategy;

    @InjectMocks
    private ClanScoreServiceImpl clanScoreService;

    private String clanId;
    private Clan dummyClan;

    @BeforeEach
    void setUp() {
        clanId = "clan-123";
        dummyClan = new Clan();
        dummyClan.setId(clanId);
        dummyClan.setName("Yomu Warriors");
        dummyClan.setScore(100);
        dummyClan.setTier(Tier.BRONZE);
    }

    @Test
    void updateClanScore_WhenClanExists_ShouldCalculateIncrementAndSave() {
        int basePoints = 10;
        int strategyPoints = 10; // Strategy returns 10
        double multiplier = 1.5; // Modifier returns 1.5x

        doNothing().when(clanValidator).requireClanId(clanId);
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(scoringStrategyResolver.getStrategy(Tier.BRONZE)).thenReturn(scoringStrategy);
        when(scoringStrategy.calculateScore(dummyClan, basePoints)).thenReturn(strategyPoints);
        when(modifierService.getActiveMultiplier(clanId)).thenReturn(multiplier);
        when(clanRepository.save(any(Clan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        clanScoreService.updateClanScore(clanId, basePoints);

        // Expected score: 100 + round(10 * 1.5) = 100 + 15 = 115
        assertAll("Verify clan score is updated correctly",
                () -> assertEquals(115, dummyClan.getScore(), "Clan score should be updated with strategy points and multiplier applied"),
                () -> verify(clanRepository).save(dummyClan));
    }

    @Test
    void updateClanScore_WhenClanDoesNotExist_ShouldThrowException() {
        doNothing().when(clanValidator).requireClanId(clanId);
        when(clanRepository.findById(clanId)).thenReturn(Optional.empty());

        assertAll("Verify update clan score throws exception when clan not found",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> clanScoreService.updateClanScore(clanId, 10),
                            "Should throw IllegalArgumentException");
                    assertEquals(SocialConstants.CLAN_NOT_FOUND_MESSAGE, ex.getMessage(), "Message should match");
                });
    }
}
