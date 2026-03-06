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
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Hanya Admin yang dapat membuat teks bacaan.");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        ReadingText text = ReadingText.builder()
                .title(request.title())
                .content(request.content())
                .category(category)
                .build();

        ReadingText savedText = readingTextRepository.save(text);

        return new ReadingTextResponse(
                savedText.getId(),
                savedText.getTitle(),
                savedText.getContent(),
                category.getName()
        );
    }

    @Override
    public List<ReadingTextResponse> getAllTexts() {
        return readingTextRepository.findAll().stream()
                .map(text -> new ReadingTextResponse(
                        text.getId(),
                        text.getTitle(),
                        text.getContent(),
                        text.getCategory().getName()
                ))
                .toList();
    }

    @Override
    public void deleteText(Long id, String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Hanya Admin yang dapat menghapus teks bacaan.");
        }

        if (!readingTextRepository.existsById(id)) {
            throw new RuntimeException("Teks bacaan tidak ditemukan.");
        }

        readingTextRepository.deleteById(id);
    }
}