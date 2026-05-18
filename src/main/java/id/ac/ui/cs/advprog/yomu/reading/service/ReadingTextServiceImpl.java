package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextResponse;
import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.CategoryRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import id.ac.ui.cs.advprog.yomu.reading.event.ReadingCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingTextServiceImpl implements ReadingTextService {

    private final ReadingTextRepository readingTextRepository;
    private final CategoryRepository categoryRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ReadingTextResponse createText(ReadingTextRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        ReadingText readingText = new ReadingText();
        readingText.setTitle(request.title());
        readingText.setContent(request.content());
        readingText.setCategory(category);

        ReadingText saved = readingTextRepository.save(readingText);
        return new ReadingTextResponse(saved.getId(), saved.getTitle(), saved.getContent(), saved.getCategory().getName());
    }

    @Override
    public List<ReadingTextResponse> getAllTexts() {
        return readingTextRepository.findAll().stream()
                .map(t -> new ReadingTextResponse(t.getId(), t.getTitle(), t.getContent(), t.getCategory().getName()))
                .collect(Collectors.toList());
    }

    @Override
    public ReadingTextResponse getTextById(Long id) {
        ReadingText text = readingTextRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reading text not found"));
        return new ReadingTextResponse(text.getId(), text.getTitle(), text.getContent(), text.getCategory().getName());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ReadingTextResponse updateText(Long id, ReadingTextRequest request) {
        ReadingText readingText = readingTextRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reading text not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        readingText.setTitle(request.title());
        readingText.setContent(request.content());
        readingText.setCategory(category);

        ReadingText saved = readingTextRepository.save(readingText);
        return new ReadingTextResponse(saved.getId(), saved.getTitle(), saved.getContent(), saved.getCategory().getName());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteText(Long id) {
        if (!readingTextRepository.existsById(id)) {
            throw new RuntimeException("Reading text not found");
        }
        readingTextRepository.deleteById(id);
    }

    @Override
    public void completeReading(Long id, String username) {
        readingTextRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reading text not found"));

        ReadingCompletedEvent event = new ReadingCompletedEvent(this, id, username);
        eventPublisher.publishEvent(event);
    }
}