package org.example.Service.Implementation;

import org.example.Domain.Entities.Quest;
import org.example.Domain.Entities.User;
import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.Quest.Request.CreateQuestRequest;
import org.example.Domain.Models.Quest.Response.GetAllQuestsResponse;
import org.example.Domain.Models.Quest.Response.GetQuestResponse;
import org.example.Repository.BadgesRepository;
import org.example.Repository.QuestsRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.IQuestService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestsService implements IQuestService {
    private final QuestsRepository questsRepository;
    private final UsersRepository usersRepository;
    private final BadgesRepository badgesRepository;
    private final Mapper mapper;

    public QuestsService(QuestsRepository questsRepository, UsersRepository usersRepository, BadgesRepository badgesRepository, Mapper mapper) {
        this.questsRepository = questsRepository;
        this.usersRepository = usersRepository;
        this.badgesRepository = badgesRepository;
        this.mapper = mapper;
    }

    @Override
    public Quest createQuest(CreateQuestRequest createQuestRequest, int idUser) {
        User user = usersRepository.getById(idUser);
        if(user.getTokens()<createQuestRequest.getRewardTokens())
            throw new RuntimeException("Not enough tokens");
        user.setTokens(user.getTokens() - createQuestRequest.getRewardTokens());
        return questsRepository.save(mapper.CreateQuestRequestToQuest(createQuestRequest));
    }

    @Override
    public Quest updateQuest(Quest quests, Integer id) {
        Quest questFromDb = questsRepository.getById(id);
        questFromDb.setAnswer(quests.getAnswer());
        questFromDb.setDescription(quests.getDescription());
        questFromDb.setQuestRewardTokens(quests.getQuestRewardTokens());
        return questsRepository.save(questFromDb);
    }

    @Override
    public void deleteQuest(Integer id) {
        questsRepository.deleteById(id);
    }

    @Override
    public GetQuestResponse findQuestById(Integer id) {
        Quest quest = questsRepository.getById(id);
        return mapper.QuestToGetQuestResponse(quest);
    }

    @Override
    public Iterable<GetAllQuestsResponse> findAllQuests() {
        List<GetAllQuestsResponse> list = new ArrayList<>();
        questsRepository.findAll().forEach(quest -> {
            list.add(mapper.QuestToGetAllQuestResponse(quest));
        });
        return list;
    }

    @Override
    public GetQuestResponse resolveQuest(int idQuest, int idUser) {
        Quest quest = questsRepository.getById(idQuest);
        User user = usersRepository.getById(idUser);
        user.setThreshold(user.getThreshold() + quest.getThreshold());
        user.setTokens(user.getTokens() + quest.getQuestRewardTokens());
        quest.setRewarded(true);
        quest.getUsers1().add(user);
        user.getQuestsList().add(quest);
        usersRepository.save(user);
        questsRepository.save(quest);
        return mapper.QuestToGetQuestResponse(quest);
    }

    @Override
    public void updateRewarded(int idQuest, boolean rewarded) {
        Quest quest = questsRepository.getById(idQuest);
        quest.setRewarded(rewarded);
        questsRepository.save(quest);
    }

    @Override
    public boolean checkAnswer(int userId, String answer, int questId) {
        Quest quest = questsRepository.getById(questId);
        User user = usersRepository.getById(userId);
        if (quest.getAnswer().equalsIgnoreCase(answer)) {
            quest.getUsers1().add(user);
            user.getQuestsList().add(quest);
            usersRepository.save(user);
            questsRepository.save(quest);
            return true;
        }
        return false;

    }
}
