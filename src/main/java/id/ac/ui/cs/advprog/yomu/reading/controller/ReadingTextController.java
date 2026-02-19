package id.ac.ui.cs.advprog.yomu.reading.controller;

import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.service.ReadingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
@RequestMapping("/api/readings")
@CrossOrigin(origins = "*") // Izinkan Frontend akses
public class ReadingTextController {

    @Autowired
    private ReadingService repository;

    @GetMapping
    public List<ReadingText> getAllReadings() {
        return repository.findAll();
    }
}