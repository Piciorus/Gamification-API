package org.example.Domain.Mapper;

import org.example.Domain.Entities.Badge;
import org.example.Domain.Entities.Quest;
import org.example.Domain.Entities.Question;
import org.example.Domain.Entities.User;
import org.example.Domain.Models.Badge.Request.CreateBadgeRequest;
import org.example.Domain.Models.Badge.Response.GetAllBadgesResponse;
import org.example.Domain.Models.Badge.Response.GetBadgeByIdResponse;
import org.example.Domain.Models.Quest.Request.CreateQuestRequest;
import org.example.Domain.Models.Quest.Request.UpdateQuestRequest;
import org.example.Domain.Models.Quest.Response.GetAllQuestsResponse;
import org.example.Domain.Models.Quest.Response.GetQuestResponse;
import org.example.Domain.Models.Question.Request.CreateQuestionRequest;
import org.example.Domain.Models.Question.Response.GetAllQuestionsResponse;
import org.example.Domain.Models.Question.Request.UpdateQuestionRequest;
import org.example.Domain.Models.User.Response.GetAllUsersResponse;
import org.example.Domain.Models.User.Response.GetUserByIdResponse;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Mapper {

    public Quest CreateQuestRequestToQuest(CreateQuestRequest createQuestRequest) {
        Quest quest = new Quest();
        quest.setAnswer(createQuestRequest.getAnswer());
        quest.setDescription(createQuestRequest.getDescription());
        quest.setQuestRewardTokens(createQuestRequest.getRewardTokens());
        quest.setDifficulty(createQuestRequest.getDifficulty());
        quest.setThreshold(createQuestRequest.getThreshold());
        quest.setCreationDate(new Date());
        return quest;
    }

    public GetAllUsersResponse UserToGetAllUsersResponse(User user) {
        GetAllUsersResponse user1 = new GetAllUsersResponse();
        user1.setUsername(user.getUsername());
        user1.setRoles(user.getRoles());
        user1.setEmail(user.getEmail());
        user1.setTokens(user.getTokens());
        user1.setThreshold(user.getThreshold());
        user1.setId(user.getId());
        return user1;
    }

    public GetUserByIdResponse UserToGetUserByIdResponse(User user) {
        GetUserByIdResponse user1 = new GetUserByIdResponse();
        user1.setUsername(user.getUsername());
        user1.setRoles(user.getRoles());
        user1.setEmail(user.getEmail());
        user1.setTokens(user.getTokens());
        user1.setThreshold(user.getThreshold());
        user1.setId(user.getId());
        return user1;
    }

    public GetAllQuestsResponse QuestToGetAllQuestResponse(Quest quest) {
        GetAllQuestsResponse quest1 = new GetAllQuestsResponse();
        quest1.setId(quest.getId());
        quest1.setAnswer(quest.getAnswer());
        quest1.setDescription(quest.getDescription());
        quest1.setQuestRewardTokens(quest.getQuestRewardTokens());
        quest1.setDifficulty(quest.getDifficulty());
        quest1.setThreshold(quest.getThreshold());
        quest1.setRewarded(quest.isRewarded());
        return quest1;
    }

    public GetAllBadgesResponse BadgeToGetAllBadgesResponse(Badge badge) {
        GetAllBadgesResponse badge1 = new GetAllBadgesResponse();
        badge1.setName(badge.getName());
        badge1.setId(badge.getId());
        return badge1;
    }

    public GetBadgeByIdResponse BadgeToGetByIdBadgeResponse(Badge badge) {
        GetBadgeByIdResponse badge1 = new GetBadgeByIdResponse();
        badge1.setName(badge.getName());
        badge1.setId(badge.getId());
        return badge1;
    }

    public GetQuestResponse QuestToGetQuestResponse(Quest quest) {
        GetQuestResponse quest1 = new GetQuestResponse();
        quest1.setId(quest.getId());
        quest1.setAnswer(quest.getAnswer());
        quest1.setDescription(quest.getDescription());
        quest1.setQuestRewardTokens(quest.getQuestRewardTokens());
        quest1.setDifficulty(quest.getDifficulty());
        quest1.setThreshold(quest.getThreshold());
        quest1.setRewarded(quest.isRewarded());
        return quest1;
    }

    public Badge CreateBadgeToBadgeRequest(CreateBadgeRequest createBadgeRequest) {
        Badge badge = new Badge();
        badge.setName(createBadgeRequest.getName());
        badge.setCreationDate(new Date());
        return badge;
    }

    public void UpdateQuestRequestToQuest(UpdateQuestRequest updateQuestRequest, Quest quest) {
        quest.setAnswer(updateQuestRequest.getAnswer());
        quest.setQuestRewardTokens(updateQuestRequest.getQuestRewardTokens());
        quest.setThreshold(updateQuestRequest.getThreshold());
        quest.setDescription(updateQuestRequest.getDescription());
        quest.setRewarded(updateQuestRequest.isRewarded());
        quest.setUpdateDate(new Date());
    }

    public GetAllQuestionsResponse QuestionToGetAllQuestionsResponse(Question question) {
        GetAllQuestionsResponse response = new GetAllQuestionsResponse();
        response.setId(question.getId());
        response.setQuestionText(question.getQuestionText());
        response.setAnswer1(question.getAnswer1());
        response.setAnswer2(question.getAnswer2());
        response.setAnswer3(question.getAnswer3());
        response.setCorrectAnswer(question.getCorrectAnswer());
        response.setRewarded(question.isRewarded());
        response.setDifficulty(question.getDifficulty());
        response.setThreshold(question.getThreshold());
        response.setQuestRewardTokens(question.getQuestRewardTokens());
        response.setCategory(question.getCategory());
        response.setCheckByAdmin(question.isCheckByAdmin());
        return response;
    }

    public Question CreateQuestionRequestToQuestion(CreateQuestionRequest createQuestionRequest) {
        Question quest = new Question();
        quest.setQuestionText(createQuestionRequest.getQuestionText());
        quest.setAnswer1(createQuestionRequest.getAnswer1());
        quest.setAnswer2(createQuestionRequest.getAnswer2());
        quest.setAnswer3(createQuestionRequest.getAnswer3());
        quest.setCorrectAnswer(createQuestionRequest.getCorrectAnswer());
        quest.setDifficulty(createQuestionRequest.getDifficulty());
        quest.setQuestRewardTokens(createQuestionRequest.getQuestRewardTokens());
        quest.setThreshold(createQuestionRequest.getThreshold());
        return quest;
    }

    public void UpdateQuestionRequestToQuestion(UpdateQuestionRequest updateQuestionRequest,Question question) {
        question.setQuestionText(updateQuestionRequest.getQuestionText());
        question.setAnswer1(updateQuestionRequest.getAnswer1());
        question.setAnswer2(updateQuestionRequest.getAnswer2());
        question.setAnswer3(updateQuestionRequest.getAnswer3());
        question.setDifficulty(updateQuestionRequest.getDifficulty());
        question.setCheckByAdmin(updateQuestionRequest.isCheckedByAdmin());
        question.setThreshold(updateQuestionRequest.getThreshold());
        question.setCorrectAnswer(updateQuestionRequest.getCorrectAnswer());
        question.setQuestRewardTokens(updateQuestionRequest.getQuestRewardTokens());
        question.setUpdateDate(new Date());
    }

}
