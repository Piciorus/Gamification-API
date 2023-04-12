package org.example.Service.Interfaces;

import org.example.Domain.Entities.Quest;
import org.example.Domain.Models.Quest.Request.CreateQuestRequest;
import org.example.Domain.Models.Quest.Response.GetAllQuestsResponse;
import org.example.Domain.Models.Quest.Response.GetQuestResponse;

public interface IQuestService {

    Quest createQuest(CreateQuestRequest createQuestRequest, int UserId);

    Quest updateQuest(Quest quests, Integer id);

    void deleteQuest(Integer id);

    GetQuestResponse findQuestById(Integer id);

    Iterable<GetAllQuestsResponse> findAllQuests();

    GetQuestResponse resolveQuest(int idQuest, int idUser);

    void updateRewarded(int idQuest, boolean rewarded);

    boolean checkAnswer(int userId, String answer, int questId);

}
