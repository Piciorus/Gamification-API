package org.example.Service.Interfaces;

import org.example.Domain.Entities.Quest;

public interface IQuestService {

    Quest createQuest(Quest quest);

    Quest updateQuest(Quest quests, Integer id);

    void deleteQuest(Integer id);

    Quest findQuestById(Integer id);

    Iterable<Quest> findAllQuests();

    Quest resolveQuest(int idQuest,int idUser);

    void updateRewarded(int idQuest, boolean rewarded);

}
