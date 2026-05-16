package id.ac.ui.cs.advprog.yomu.reading.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts") // PMD Fix: Mengizinkan lebih dari 1 assert
class ReadingTextTest {

    @Test
    void testCustomConstructorInitializesEmptyLists() {
        // Menguji custom constructor 4 parameter yang kita buat untuk menghindari NullPointerException
        Category category = new Category(1L, "Sains");
        ReadingText text = new ReadingText(10L, "Fisika Quantum", "Materi Fisika", category);

        // PMD Fix: Menambahkan pesan penjelasan pada setiap assert
        assertEquals(10L, text.getId(), "ID teks bacaan harus sesuai dengan parameter konstruktor");
        assertEquals("Fisika Quantum", text.getTitle(), "Judul teks bacaan harus sesuai dengan parameter konstruktor");
        assertEquals("Sains", text.getCategory().getName(), "Kategori teks bacaan harus sesuai dengan parameter konstruktor");

        // Verifikasi bahwa list otomatis diinisialisasi sebagai list kosong, bukan null
        assertNotNull(text.getQuestions(), "List pertanyaan tidak boleh null");
        assertNotNull(text.getCompletions(), "List completion tidak boleh null");
        assertTrue(text.getQuestions().isEmpty(), "List pertanyaan harus kosong di awal");
        assertTrue(text.getCompletions().isEmpty(), "List completion harus kosong di awal");
    }
}