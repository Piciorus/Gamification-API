package org.example.Controller;

import org.example.Domain.Entities.Leaderboard;
import org.example.Service.Interfaces.ILeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController()
@Component
public class LeaderboardController {
    private final ILeaderboardService leaderboardService;

    @Autowired
    public LeaderboardController(ILeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping(path = "/getLeaderboardById/{id}")
    public ResponseEntity<Leaderboard> getLeaderboardById(@PathVariable("id") Integer leaderboardId) {
        return ResponseEntity.ok(leaderboardService.findLeaderboardById(leaderboardId));
    }

    @GetMapping(path = "/getAllLeaderboards")
    public ResponseEntity<Iterable<Leaderboard>> getAllLeaderboards() {
        return ResponseEntity.ok(leaderboardService.findAllLeaderboards());
    }

    @PostMapping(path = "/createLeaderboard/{id}")
    public ResponseEntity createLeaderboard(@RequestBody final Leaderboard leaderboards, @PathVariable("id") Integer userId) {
        leaderboardService.createLeaderboard(leaderboards, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/updateLeaderboard")
    public ResponseEntity updateLeaderboard(@RequestBody final Leaderboard leaderboards, @PathVariable("id") Integer leaderboardId) {
        leaderboardService.updateLeaderboard(leaderboards, leaderboardId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/deleteLeaderboard")
    public ResponseEntity deleteLeaderboard(@PathVariable("id") Integer leaderboardId) {
        leaderboardService.deleteLeaderboard(leaderboardId);
        return ResponseEntity.ok().build();
    }

}
