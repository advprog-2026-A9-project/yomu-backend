package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import java.util.List;

public interface ReadingService {
    List<ReadingText> findAll();
    ReadingText create(ReadingText readingText);

}