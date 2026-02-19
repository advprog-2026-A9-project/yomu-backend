package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReadingServiceImpl implements ReadingService {

    @Autowired
    private ReadingTextRepository repository;

    @Override
    public List<ReadingText> findAll() {
        seedData(); // Cek seed setiap kali fetch (untuk demo)
        return repository.findAll();
    }

    @Override
    public ReadingText create(ReadingText readingText) {
        return repository.save(readingText);
    }

    @Override
    public void seedData() {
        if (repository.count() == 0) {
            repository.save(new ReadingText(null, "Modular Monolith 101", "Panduan arsitektur.", "Teknologi"));
            repository.save(new ReadingText(null, "Sejarah Fasilkom", "Gedung baru vs lama.", "Sejarah"));
        }
    }
}