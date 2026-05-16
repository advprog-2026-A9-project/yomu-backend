package id.ac.ui.cs.advprog.yomu.reading.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void testCategoryGettersAndSetters() {
        // Memastikan Lombok @Getter dan @Setter bekerja dengan baik
        Category category = new Category();
        category.setId(1L);
        category.setName("Sejarah");

        assertEquals(1L, category.getId());
        assertEquals("Sejarah", category.getName());
        assertNull(category.getReadingTexts(), "List reading texts seharusnya null jika belum di-set");
    }
}