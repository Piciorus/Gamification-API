package org.example.Service.Interfaces;

import org.example.Domain.Entities.Quest;
import org.example.Domain.Models.Quest.Request.CreateQuestRequest;
import org.example.Domain.Models.Quest.Request.UpdateQuestRequest;
import org.example.Domain.Models.Quest.Response.GetAllQuestsResponse;
import org.example.Domain.Models.Quest.Response.GetQuestResponse;

import java.util.UUID;

public interface IQuestService {

    Quest createQuest(CreateQuestRequest createQuestRequest, UUID idUser);

    Quest updateQuest(UpdateQuestRequest updateQuestRequest, UUID idQuest);

    void deleteQuest(UUID idQuest);

    GetQuestResponse findQuestById(UUID idQuest);

    Iterable<GetAllQuestsResponse> findAllQuests();

    Iterable<GetAllQuestsResponse> findAllResolvedQuestsByUserId(UUID idUser);

    GetQuestResponse resolveQuest(UUID idQuest, UUID idUser);

    boolean checkAnswer(UUID userId, String answer, UUID questId);

}
