package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextResponse;
import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.CategoryRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadingTextServiceImpl implements ReadingTextService {

    private final ReadingTextRepository readingTextRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ReadingTextResponse createText(ReadingTextRequest request, String role) {
        // Logika dummy auth: Tolak jika bukan ADMIN
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Hanya Admin yang dapat membuat teks bacaan.");
        }

        // Cari kategori dari database
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        // Buat objek teks bacaan baru
        ReadingText text = ReadingText.builder()
                .title(request.title())
                .content(request.content())
                .category(category)
                .build();

        // Simpan ke database
        ReadingText savedText = readingTextRepository.save(text);

        // Kembalikan DTO response
        return new ReadingTextResponse(
                savedText.getId(),
                savedText.getTitle(),
                savedText.getContent(),
                category.getName()
        );
    }

    @Override
    public List<ReadingTextResponse> getAllTexts() {
        // STUB: Sengaja return null agar test gagal (Fase RED)
        return null;
    }

    @Override
    public void deleteText(Long id, String role) {
        // STUB: Sengaja kosong agar tidak melakukan apa-apa (Fase RED)
    }
}