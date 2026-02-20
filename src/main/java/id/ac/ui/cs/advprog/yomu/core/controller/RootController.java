package id.ac.ui.cs.advprog.yomu.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*") // CORS
public class RootController {

    @GetMapping("/")
    public String index() {
        return "Yomu Backend is Running Successfully!";
    }
}