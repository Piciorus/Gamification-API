package org.example.Controller;

import org.example.Domain.Models.Badge.Request.CreateBadgeRequest;
import org.example.Domain.Models.Badge.Response.GetAllBadgesResponse;
import org.example.Domain.Models.Badge.Response.GetBadgeByIdResponse;
import org.example.Service.Interfaces.IBadgesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/badge")
public class BadgeController {
    private final IBadgesService badgeService;

    @Autowired
    public BadgeController(IBadgesService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping(path = "/getAllBadges")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Iterable<GetAllBadgesResponse>> getAllBadges() {
        return ResponseEntity.ok(badgeService.findAllBadges());
    }

    @GetMapping(path = "/getBadgeById/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<GetBadgeByIdResponse> getBadgeById(@PathVariable("id") @NotBlank UUID badgeId) {
        return ResponseEntity.ok(badgeService.findBadgeById(badgeId));
    }

    @PostMapping(path = "/createBadge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity createBadge(@Valid @RequestBody final CreateBadgeRequest createBadgeRequest) {
        badgeService.createBadge(createBadgeRequest);
        return ResponseEntity.ok().body("Badge created!");
    }

    @DeleteMapping(path = "/deleteBadge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity deleteBadge(@PathVariable("id") @NotBlank UUID badgeId) {
        badgeService.deleteBadge(badgeId);
        return ResponseEntity.ok().body("Badge deleted!");
    }

    @PostMapping(path = "/rewardBadge/{idBadge}/{idUser}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public void rewardBadge(@PathVariable("idBadge") @NotBlank UUID idBadge, @PathVariable("idUser") @NotBlank UUID idUser) {
        badgeService.rewardBadge(idBadge, idUser);
    }

    @GetMapping(path = "/getBadgesByUserId/{idUser}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Iterable<GetBadgeByIdResponse>> getBadgesByUserId(@PathVariable("idUser") @NotBlank UUID idUser) {
        return ResponseEntity.ok(badgeService.findBadgesByUserId(idUser));
    }
}
