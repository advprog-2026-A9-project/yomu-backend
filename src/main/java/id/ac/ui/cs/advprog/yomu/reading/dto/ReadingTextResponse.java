package id.ac.ui.cs.advprog.yomu.reading.dto;

public record ReadingTextResponse(
        Long id,
        String title,
        String content,
        String categoryName
) {}