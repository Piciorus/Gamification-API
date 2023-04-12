package org.example.Controller;

import org.example.Domain.Entities.Quest;
import org.example.Domain.Models.Quest.Request.CheckAnswerRequest;
import org.example.Domain.Models.Quest.Request.CreateQuestRequest;
import org.example.Domain.Models.Quest.Response.GetAllQuestsResponse;
import org.example.Domain.Models.Quest.Response.GetQuestResponse;
import org.example.Service.Interfaces.IQuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController()
@Component
public class QuestController {
    private final IQuestService questService;

    @Autowired
    public QuestController(IQuestService questService) {
        this.questService = questService;
    }

    @PostMapping(path = "/createQuest/{id}")
    public void createQuest(@RequestBody final CreateQuestRequest createQuestRequest, @PathVariable("id") Integer UserId) {
        questService.createQuest(createQuestRequest, UserId);
    }

    @PutMapping(path = "/updateQuest")
    public ResponseEntity updateQuest(@RequestBody final Quest quests, @PathVariable("id") Integer QuestId) {
        questService.updateQuest(quests, QuestId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/deleteQuest/{id}")
    public ResponseEntity deleteQuest(@PathVariable("id") Integer QuestId) {
        questService.deleteQuest(QuestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/getQuestById/{id}")
    public ResponseEntity<GetQuestResponse> getQuestById(@PathVariable("id") Integer QuestId) {
        return ResponseEntity.ok(questService.findQuestById(QuestId));
    }

    @GetMapping(path = "/getAllQuests")
    public ResponseEntity<Iterable<GetAllQuestsResponse>> getAllQuests() {
        return ResponseEntity.ok(questService.findAllQuests());
    }

    @PostMapping(path = "/resolveQuest/{idQuest}/{idUser}")
    public GetQuestResponse resolveQuest(@PathVariable("idQuest") Integer idQuest, @PathVariable("idUser") Integer idUser) {
        return questService.resolveQuest(idQuest, idUser);
    }

    @PutMapping(path = "/updateRewarded/{idQuest}")
    public void updateRewarded(@PathVariable("idQuest") Integer idQuest, @RequestBody boolean rewarded) {
        questService.updateRewarded(idQuest, rewarded);
    }

    @PostMapping(path = "/checkAnswer")
    public boolean checkAnswer(@RequestBody CheckAnswerRequest checkAnswerRequest) {
        return questService.checkAnswer(checkAnswerRequest.getUserId(), checkAnswerRequest.getAnswer(), checkAnswerRequest.getQuestId());
    }
}
