package id.ac.ui.cs.advprog.yomu.reading.event;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
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

        assertEquals(userId, event.userId(), "User ID harus sesuai dengan input");
        assertEquals(textId, event.readingTextId(), "Reading Text ID harus sesuai dengan input");
        assertEquals(score, event.score(), "Score harus sesuai dengan input");
        assertEquals(correct, event.correctAnswers(), "Jumlah jawaban benar harus sesuai dengan input");
        assertEquals(total, event.totalQuestions(), "Total pertanyaan harus sesuai dengan input");
    }
}