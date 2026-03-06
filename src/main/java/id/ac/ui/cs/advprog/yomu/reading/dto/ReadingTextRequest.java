package id.ac.ui.cs.advprog.yomu.reading.dto;

public record ReadingTextRequest(
        String title,
        String content,
        Long categoryId
) {}