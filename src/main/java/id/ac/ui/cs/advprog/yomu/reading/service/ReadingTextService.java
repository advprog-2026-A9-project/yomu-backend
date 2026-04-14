package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextResponse;

import java.util.List;

public interface ReadingTextService {

    // Fitur: Admin dapat membuat teks bacaan
    ReadingTextResponse createText(ReadingTextRequest request, String role);

    // Fitur: Pelajar dapat melihat daftar teks yang tersedia
    List<ReadingTextResponse> getAllTexts();

    // Fitur: Admin dapat menghapus teks bacaan
    void deleteText(Long id, String role);

}