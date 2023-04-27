package org.example.Controller;

import org.example.Domain.Models.Quest.Request.CheckAnswerRequest;
import org.example.Domain.Models.Quest.Request.CreateQuestRequest;
import org.example.Domain.Models.Quest.Request.UpdateQuestRequest;
import org.example.Domain.Models.Quest.Response.GetAllQuestsResponse;
import org.example.Domain.Models.Quest.Response.GetQuestResponse;
import org.example.Service.Interfaces.IQuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/quest")
public class QuestController {
    private final IQuestService questService;

    @Autowired
    public QuestController(IQuestService questService) {
        this.questService = questService;
    }

    @PostMapping(path = "/createQuest/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity createQuest(@Valid @RequestBody final CreateQuestRequest createQuestRequest, @PathVariable("id") @NotBlank UUID idUser) {
        questService.createQuest(createQuestRequest, idUser);
        return ResponseEntity.ok().body("Quest created!");
    }

    @PutMapping(path = "/updateQuest/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity updateQuest(@Valid @RequestBody final UpdateQuestRequest updateQuestRequest, @PathVariable("id") @NotBlank UUID idQuest) {
        questService.updateQuest(updateQuestRequest, idQuest);
        return ResponseEntity.ok().body("Quest updated!");
    }

    @DeleteMapping(path = "/deleteQuest/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity deleteQuest(@PathVariable("id") @NotBlank UUID idQuest) {
        questService.deleteQuest(idQuest);
        return ResponseEntity.ok().body("Quest deleted!");
    }

    @GetMapping(path = "/getQuestById/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<GetQuestResponse> getQuestById(@PathVariable("id") @NotBlank UUID idQuest) {
        return ResponseEntity.ok(questService.findQuestById(idQuest));
    }

    @GetMapping(path = "/getAllQuests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Iterable<GetAllQuestsResponse>> getAllQuests() {
        return ResponseEntity.ok(questService.findAllQuests());
    }

    @PostMapping(path = "/resolveQuest/{idQuest}/{idUser}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public GetQuestResponse resolveQuest(@PathVariable("idQuest") @NotBlank UUID idQuest, @PathVariable("idUser") @NotBlank UUID idUser) {
        return questService.resolveQuest(idQuest, idUser);
    }

    @PostMapping(path = "/checkAnswer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public boolean checkAnswer(@Valid @RequestBody CheckAnswerRequest checkAnswerRequest) {
        return questService.checkAnswer(checkAnswerRequest.getUserId(), checkAnswerRequest.getAnswer(), checkAnswerRequest.getQuestId());
    }
}
