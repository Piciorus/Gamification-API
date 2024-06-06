package org.example.Domain.Mapper;

import org.example.Domain.Entities.Badge;
import org.example.Domain.Entities.Category;
import org.example.Domain.Entities.Question;
import org.example.Domain.Entities.User;
import org.example.Domain.Entities.UserQuestionHistory;
import org.example.Domain.Entities.UserTestsHistory;
import org.example.Domain.Models.Badge.Request.CreateBadgeRequest;
import org.example.Domain.Models.Badge.Response.GetAllBadgesResponse;
import org.example.Domain.Models.Badge.Response.GetBadgeByIdResponse;
import org.example.Domain.Models.Category.GetAllCategoriesResponse;
import org.example.Domain.Models.Question.Request.CreateQuestionRequest;
import org.example.Domain.Models.Question.Request.SaveTestHistoryRequest;
import org.example.Domain.Models.Question.Request.UpdateQuestionRequest;
import org.example.Domain.Models.Question.Response.GetAllQuestionsHistoryUserResponse;
import org.example.Domain.Models.Question.Response.GetAllQuestionsResponse;
import org.example.Domain.Models.Question.Response.GetAllTestsHistoryResponse;
import org.example.Domain.Models.User.Request.UpdateUserRequest;
import org.example.Domain.Models.User.Response.GetAllUsersResponse;
import org.example.Domain.Models.User.Response.GetUserByIdResponse;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Mapper {
    public GetAllUsersResponse UserToGetAllUsersResponse(User user) {
        GetAllUsersResponse user1 = new GetAllUsersResponse();
        user1.setUsername(user.getUsername());
        user1.setRoles(user.getRoles());
        user1.setEmail(user.getEmail());
        user1.setTokens(user.getTokens());
        user1.setThreshold(user.getThreshold());
        user1.setId(user.getId());
        user1.setBadges(user.getBadgesList());
        user1.setAvatar(user.getAvatar());
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

    public Badge CreateBadgeToBadgeRequest(CreateBadgeRequest createBadgeRequest) {
        Badge badge = new Badge();
        badge.setName(createBadgeRequest.getName());
        badge.setCreationDate(new Date());
        return badge;
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

    public void UpdateQuestionRequestToQuestion(UpdateQuestionRequest updateQuestionRequest, Question question) {
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

    public void UpdateUserRequestToUser(UpdateUserRequest updateUserRequest, User user) {
        user.setUsername(updateUserRequest.getUsername());
        user.setThreshold(updateUserRequest.getThreshold());
        user.setTokens(updateUserRequest.getTokens());
    }

    public GetAllCategoriesResponse CategoryToGetAllCategoriesResponse(Category category){
        GetAllCategoriesResponse getAllCategoriesResponse = new GetAllCategoriesResponse();
        getAllCategoriesResponse.setName(category.getName());
        getAllCategoriesResponse.setId(category.getId());
        return getAllCategoriesResponse;
    }

    public GetAllQuestionsHistoryUserResponse userQuestionHistoryToResponse(UserQuestionHistory userQuestionHistory) {
        GetAllQuestionsHistoryUserResponse response = new GetAllQuestionsHistoryUserResponse();
        response.setId(userQuestionHistory.getId());
        response.setCreationDate(userQuestionHistory.getCreationDate());
        response.setUpdateDate(userQuestionHistory.getUpdateDate());
        response.setQuestion(questionToResponse(userQuestionHistory.getQuestion()));
        response.setAnswerDate(userQuestionHistory.getAnswerDate());
        response.setCorrect(userQuestionHistory.isCorrect());
        response.setUserAnswer(userQuestionHistory.getUserAnswer());
        return response;
    }

    private GetAllQuestionsResponse questionToResponse(Question question) {
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

    public GetAllTestsHistoryResponse userTestsHistoryToResponse(UserTestsHistory userTestsHistory) {
        GetAllTestsHistoryResponse response = new GetAllTestsHistoryResponse();
        response.setTestDate(userTestsHistory.getTestDate());
        response.setChatGptCorrectAnswers(userTestsHistory.getChatGptCorrectAnswers());
        response.setUserCorrectAnswers(userTestsHistory.getUserCorrectAnswers());
        response.setQuestionsAnswered(userTestsHistory.getQuestionsAnswered());
        response.setCategory(userTestsHistory.getCategory());
        return response;
    }

    public UserTestsHistory saveTestHistoryRequestToUserTestsHistory(SaveTestHistoryRequest request) {
        UserTestsHistory userTestsHistory = new UserTestsHistory();
        userTestsHistory.setTestDate(request.getTestDate());
        userTestsHistory.setChatGptCorrectAnswers(request.getChatGptCorrectAnswers());
        userTestsHistory.setUserCorrectAnswers(request.getUserCorrectAnswers());
        userTestsHistory.setQuestionsAnswered(request.getQuestionsAnswered());
        userTestsHistory.setCategory(categoryResponseToCategory(request.getCategory())); // Assuming you have a method to map category response to category entity
        return userTestsHistory;
    }

    private Category categoryResponseToCategory(GetAllCategoriesResponse categoryResponse) {
        Category category = new Category();
        category.setId(categoryResponse.getId());
        category.setName(categoryResponse.getName());
        return category;
    }
}
