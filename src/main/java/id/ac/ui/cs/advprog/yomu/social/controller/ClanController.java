package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clans")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ClanController {

    private final ClanService clanService;

    @PostMapping
    public ResponseEntity<Clan> create(@RequestBody final ClanRequest request) {
        return ResponseEntity.ok(clanService.createClan(request));
    }

    @GetMapping
    public ResponseEntity<List<Clan>> getAll() {
        return ResponseEntity.ok(clanService.findAll());
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<String> join(@PathVariable String id, @RequestBody String userId) {
        clanService.joinClan(id, userId);
        return ResponseEntity.ok("Berhasil bergabung");
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<String> leave(@PathVariable String id, @RequestBody String userId) {
        clanService.leaveClan(id, userId);
        return ResponseEntity.ok("Berhasil keluar dari clan");
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<String> delete(@PathVariable String id, @RequestBody String userId) {
        clanService.deleteClan(id, userId);
        return ResponseEntity.ok("Clan berhasil dihapus");
    }
}