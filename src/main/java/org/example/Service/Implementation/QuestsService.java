package org.example.Service.Implementation;

import org.example.Domain.Entities.Quest;
import org.example.Domain.Entities.User;
import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.Quest.Request.CreateQuestRequest;
import org.example.Domain.Models.Quest.Request.UpdateQuestRequest;
import org.example.Domain.Models.Quest.Response.GetAllQuestsResponse;
import org.example.Domain.Models.Quest.Response.GetQuestResponse;
import org.example.Repository.QuestsRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.IQuestService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class QuestsService implements IQuestService {
    private final QuestsRepository questsRepository;
    private final UsersRepository usersRepository;
    private final Mapper mapper;

    public QuestsService(QuestsRepository questsRepository, UsersRepository usersRepository, Mapper mapper) {
        this.questsRepository = questsRepository;
        this.usersRepository = usersRepository;
        this.mapper = mapper;
    }

    @Override
    public Quest createQuest(CreateQuestRequest createQuestRequest, UUID idUser) {
        User user = usersRepository.getById(idUser);
        if (user.getTokens() < createQuestRequest.getRewardTokens())
            throw new RuntimeException("Not enough tokens");
        user.setTokens(user.getTokens() - createQuestRequest.getRewardTokens());
        Quest quest = mapper.CreateQuestRequestToQuest(createQuestRequest);
        return questsRepository.save(quest);
    }

    @Override
    public Quest updateQuest(UpdateQuestRequest updateQuestRequest, UUID idQuest) {
        Quest questFromDb = questsRepository.getById(idQuest);
        mapper.UpdateQuestRequestToQuest(updateQuestRequest, questFromDb);
        return questsRepository.save(questFromDb);
    }

    @Override
    public void deleteQuest(UUID idQuest) {
        questsRepository.deleteById(idQuest);
    }

    @Override
    public GetQuestResponse findQuestById(UUID idQuest) {
        Quest quest = questsRepository.getById(idQuest);
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
    public Iterable<GetAllQuestsResponse> findAllResolvedQuestsByUserId(UUID idUser) {
        List<GetAllQuestsResponse> list = new ArrayList<>();
        questsRepository.findAllQuestsByUserId(idUser).forEach(quest -> {
            list.add(mapper.QuestToGetAllQuestResponse(quest));
        });
        return list;
    }


    @Override
    public GetQuestResponse resolveQuest(UUID idQuest, UUID idUser) {
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
    public boolean checkAnswer(UUID userId, String answer, UUID questId) {
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
