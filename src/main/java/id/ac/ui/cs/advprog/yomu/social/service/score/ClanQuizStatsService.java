package id.ac.ui.cs.advprog.yomu.social.service.score;

import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;

public interface ClanQuizStatsService {

    @Transactional
    ClanQuizStats recordQuizResult(String clanId, int correctAnswers, int totalQuestions, int score);

    double getAccuracyRatio(ClanQuizStats stats);

    @Transactional
    void resetSeasonStats();
}
