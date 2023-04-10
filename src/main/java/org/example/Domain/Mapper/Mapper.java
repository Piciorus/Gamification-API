package org.example.Domain.Mapper;

import org.example.Domain.Entities.Quest;
import org.example.Domain.Entities.User;
import org.example.Domain.Models.Quest.CreateQuestRequest;
import org.example.Domain.Models.User.Request.RegisterUserRequest;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public User RegisterUserRequestToUser(RegisterUserRequest registerUserRequest) {
        User user = new User();
        user.setPassword(registerUserRequest.getPassword());
        user.setUsername(registerUserRequest.getUsername());
        user.setEmail(registerUserRequest.getEmail());
        return user;
    }

    public Quest CreateQuestRequestToQuest(CreateQuestRequest createQuestRequest) {
        Quest quest = new Quest();
        quest.setAnswer(createQuestRequest.getAnswer());
        quest.setDescription(createQuestRequest.getDescription());
        quest.setQuestRewardTokens(createQuestRequest.getReward());
        quest.setDifficulty(createQuestRequest.getDifficulty());
        quest.setThreshold(createQuestRequest.getThreshold());
        return quest;
    }
}
