package org.example.Controller;

import org.example.Domain.Entities.Badge;
import org.example.Service.Interfaces.IBadgesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController()
@Component
public class BadgeController {
    private final IBadgesService badgeService;

    @Autowired
    public BadgeController(IBadgesService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping(path = "/getAllBadges")
    public ResponseEntity<Iterable<Badge>> getAllBadges() {
        return ResponseEntity.ok(badgeService.findAllBadges());
    }

    @GetMapping(path = "/getBadgeById/{id}")
    public ResponseEntity<Badge> getBadgeById(@PathVariable("id") Integer badgeId) {
        return ResponseEntity.ok(badgeService.findBadgeById(badgeId));
    }

    @PostMapping(path = "/createBadge")
    public ResponseEntity createBadge(@RequestBody final Badge badges) {
        badgeService.createBadge(badges);
        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/updateBadge")
    public ResponseEntity updateBadge(@RequestBody final Badge badges, @PathVariable("id") Integer badgeId) {
        badgeService.updateBadge(badges, badgeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/deleteBadge")
    public ResponseEntity deleteBadge(@PathVariable("id") Integer badgeId) {
        badgeService.deleteBadge(badgeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/rewardBadge/{idBadge}/{idUser}")
    public void rewardBadge(@PathVariable("idBadge") Integer idBadge,@PathVariable("idUser") Integer idUser) {
        badgeService.rewardBadge(idBadge, idUser);
    }

    @GetMapping(path = "/getBadgesByUserId/{idUser}")
    public ResponseEntity<Iterable<Badge>> getBadgesByUserId(@PathVariable("idUser") Integer idUser) {
        return ResponseEntity.ok(badgeService.findBadgesByUserId(idUser));
    }
}
