package id.ac.ui.cs.advprog.yomu.social.service;

import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
public interface ClanQuizStatsService {

    @Transactional
    public ClanQuizStats recordQuizResult(String clanId, int correctAnswers, int totalQuestions, int score);

    public double getAccuracyRatio(ClanQuizStats stats);

    @Transactional
    public void resetSeasonStats();
}
