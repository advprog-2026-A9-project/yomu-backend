package id.ac.ui.cs.advprog.yomu.reading.event;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuizCompletedEventTest {

    @Test
    void testQuizCompletedEventRecord() {
        // Memastikan Java Record menyimpan dan mengekspos data dengan benar untuk Event Broadcasting
        String userId = "user-123";
        Long textId = 5L;
        int score = 80;
        int correct = 4;
        int total = 5;

        QuizCompletedEvent event = new QuizCompletedEvent(userId, textId, score, correct, total);

        assertEquals(userId, event.userId());
        assertEquals(textId, event.readingTextId());
        assertEquals(score, event.score());
        assertEquals(correct, event.correctAnswers());
        assertEquals(total, event.totalQuestions());
    }
}