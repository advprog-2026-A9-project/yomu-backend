package id.ac.ui.cs.advprog.yomu.reading.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class CategoryTest {

    @Test
    void testCategoryGettersAndSetters() {
        // Memastikan Lombok @Getter dan @Setter bekerja dengan baik
        Category category = new Category();
        category.setId(1L);
        category.setName("Sejarah");

        assertEquals(1L, category.getId(), "ID kategori harus sesuai dengan yang di-set");
        assertEquals("Sejarah", category.getName(), "Nama kategori harus sesuai dengan yang di-set");
        assertNull(category.getReadingTexts(), "List reading texts seharusnya null jika belum di-set");
    }
}