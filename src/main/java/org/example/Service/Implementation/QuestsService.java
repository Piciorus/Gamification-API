package org.example.Service.Implementation;

import org.example.Domain.Entities.Quest;
import org.example.Domain.Entities.User;
import org.example.Domain.Mapper.Mapper;
import org.example.Repository.BadgesRepository;
import org.example.Repository.QuestsRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.IQuestService;
import org.springframework.stereotype.Service;

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
    public Quest createQuest(Quest quest) {
        return questsRepository.save(quest);
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
    public Quest findQuestById(Integer id) {
        return questsRepository.getById(id);
    }

    @Override
    public Iterable<Quest> findAllQuests() {
        return questsRepository.findAll();
    }

    @Override
    public Quest resolveQuest(int idQuest,int idUser) {
        Quest quest = questsRepository.getById(idQuest);
        User user = usersRepository.getById(idUser);
        quest.getUsers1().add(user);
        user.getQuestsList().add(quest);
        usersRepository.save(user);
        questsRepository.save(quest);
        return quest;
    }

    @Override
    public void updateRewarded(int idQuest, boolean rewarded) {
        Quest quest = questsRepository.getById(idQuest);
        quest.setRewarded(rewarded);
        questsRepository.save(quest);
    }
}
