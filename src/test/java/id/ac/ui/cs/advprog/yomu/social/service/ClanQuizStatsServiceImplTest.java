package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanQuizStatsRepository;

@ExtendWith(MockitoExtension.class)
class ClanQuizStatsServiceImplTest {

    @Mock
    private ClanQuizStatsRepository statsRepository;

    @InjectMocks
    private ClanQuizStatsServiceImpl statsService;

    @Test
    void recordQuizResult_WhenExistingStats_ShouldAccumulateValues() {
        ClanQuizStats existing = new ClanQuizStats();
        existing.setClanId("clan-1");
        existing.setTotalQuizAttempts(2);
        existing.setTotalCorrectAnswers(5);
        existing.setTotalQuestions(10);
        existing.setTotalScore(120);

        when(statsRepository.findById("clan-1")).thenReturn(Optional.of(existing));
        when(statsRepository.save(existing)).thenReturn(existing);

        ClanQuizStats result = statsService.recordQuizResult("clan-1", 3, 4, 80);

        assertAll("Verify accumulated stats values",
                () -> assertEquals(3, result.getTotalQuizAttempts(), "Attempts should increment"),
                () -> assertEquals(8, result.getTotalCorrectAnswers(), "Correct answers should accumulate"),
                () -> assertEquals(14, result.getTotalQuestions(), "Total questions should accumulate"),
                () -> assertEquals(200, result.getTotalScore(), "Score should accumulate"),
                () -> verify(statsRepository).save(existing));
    }

    @Test
    void recordQuizResult_WhenNoStats_ShouldCreateNewEntry() {
        when(statsRepository.findById("clan-2")).thenReturn(Optional.empty());
        when(statsRepository.save(org.mockito.ArgumentMatchers.any(ClanQuizStats.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClanQuizStats result = statsService.recordQuizResult("clan-2", 2, 5, 40);

        assertAll("Verify newly created stats values",
                () -> assertNotNull(result, "Stats should be created"),
                () -> assertEquals("clan-2", result.getClanId(), "Clan ID should be set"),
                () -> assertEquals(1, result.getTotalQuizAttempts(), "Attempts should start at 1"),
                () -> assertEquals(2, result.getTotalCorrectAnswers(), "Correct answers should set"),
                () -> assertEquals(5, result.getTotalQuestions(), "Total questions should set"),
                () -> assertEquals(40, result.getTotalScore(), "Score should set"));
    }

    @Test
    void getAccuracyRatio_WhenNoQuestions_ShouldReturnDefault() {
        ClanQuizStats stats = new ClanQuizStats();
        stats.setTotalQuestions(0);

        double ratio = statsService.getAccuracyRatio(stats);

        assertEquals(1.0d, ratio, "Accuracy should default to 1.0");
    }

    @Test
    void getAccuracyRatio_WhenQuestionsExist_ShouldCalculateRatio() {
        ClanQuizStats stats = new ClanQuizStats();
        stats.setTotalQuestions(4);
        stats.setTotalCorrectAnswers(3);

        double ratio = statsService.getAccuracyRatio(stats);

        assertEquals(0.75d, ratio, 0.0001d, "Accuracy should be correctAnswers/totalQuestions");
    }

    @Test
    void resetSeasonStats_ShouldDeleteAllRows() {
        statsService.resetSeasonStats();

        verify(statsRepository).deleteAllInBatch();
    }
}
