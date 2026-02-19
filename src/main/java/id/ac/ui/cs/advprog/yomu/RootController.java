package id.ac.ui.cs.advprog.yomu;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String root() {
        return "Yomu Backend is Running Successfully!";
    }
}