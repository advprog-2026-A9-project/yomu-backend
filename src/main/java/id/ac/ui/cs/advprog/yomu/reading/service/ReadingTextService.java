package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextResponse;

import java.util.List;

public interface ReadingTextService {
    ReadingTextResponse createText(ReadingTextRequest request);
    List<ReadingTextResponse> getAllTexts();
    void deleteText(Long id);
    ReadingTextResponse getTextById(Long id);
}