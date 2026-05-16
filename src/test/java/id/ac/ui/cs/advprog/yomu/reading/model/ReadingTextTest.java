package id.ac.ui.cs.advprog.yomu.reading.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReadingTextTest {

    @Test
    void testCustomConstructorInitializesEmptyLists() {
        // Menguji custom constructor 4 parameter yang kita buat untuk menghindari NullPointerException
        Category category = new Category(1L, "Sains");
        ReadingText text = new ReadingText(10L, "Fisika Quantum", "Materi Fisika", category);

        assertEquals(10L, text.getId());
        assertEquals("Fisika Quantum", text.getTitle());
        assertEquals("Sains", text.getCategory().getName());

        // Verifikasi bahwa list otomatis diinisialisasi sebagai list kosong, bukan null
        assertNotNull(text.getQuestions(), "List pertanyaan tidak boleh null");
        assertNotNull(text.getCompletions(), "List completion tidak boleh null");
        assertTrue(text.getQuestions().isEmpty(), "List pertanyaan harus kosong di awal");
        assertTrue(text.getCompletions().isEmpty(), "List completion harus kosong di awal");
    }
}