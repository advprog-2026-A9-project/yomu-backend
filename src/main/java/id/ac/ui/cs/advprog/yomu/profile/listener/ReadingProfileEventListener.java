package id.ac.ui.cs.advprog.yomu.profile.listener;

import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.reading.event.ReadingCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReadingProfileEventListener {

    private final ProfileRepository profileRepository;
    private final ProfileService profileService;

    @EventListener
    @Transactional
    public void onQuizCompleted(QuizCompletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling QuizCompletedEvent for user: {}", event.userId());
        }
        Profile profile = profileService.getOrCreateProfile(event.userId());
        
        profile.setCorrectAnswersSum(profile.getCorrectAnswersSum() + event.correctAnswers());
        profile.setTotalQuestionsSum(profile.getTotalQuestionsSum() + event.totalQuestions());
        
        if (profile.getTotalQuestionsSum() > 0) {
            profile.setQuizAccuracy((int) Math.round((profile.getCorrectAnswersSum() * 100.0) / profile.getTotalQuestionsSum()));
        }
        
        profileRepository.save(profile);
    }

    @EventListener
    @Transactional
    public void onReadingCompleted(ReadingCompletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling ReadingCompletedEvent for user: {}", event.getUsername());
        }
        Profile profile = profileService.getOrCreateProfile(event.getUsername());
        
        int newCount = profile.getCompletedTexts() + 1;
        profile.setCompletedTexts(newCount);
        profile.setTotalMinutes(newCount * 8);
        
        profileRepository.save(profile);
    }
}
