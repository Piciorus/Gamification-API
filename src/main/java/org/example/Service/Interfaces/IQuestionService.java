package org.example.Service.Interfaces;

import org.example.Domain.Entities.Question;
import org.example.Domain.Models.Question.Request.CreateQuestionRequest;
import org.example.Domain.Models.Question.Request.UpdateQuestionRequest;
import org.example.Domain.Models.Question.Request.UserAnswerRequest;
import org.example.Domain.Models.Question.Response.GetAllQuestionsHistoryUserResponse;
import org.example.Domain.Models.Question.Response.GetAllQuestionsResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IQuestionService {
    List<GetAllQuestionsResponse> getQuestionsForTest(String category, String difficulty);

    Question createQuestion(CreateQuestionRequest createQuestionRequest);

    Question updateQuestion(UpdateQuestionRequest updateQuestionRequest, UUID idQuestion);

    void resolveQuestion(UUID idQuest, UUID idUser);

    Map<String, Long> getCountOfCorrectAnswersForEachCategory();

    void markAnswerAsCorrect(UUID userId, UUID questionId, UserAnswerRequest userAnswer);

    List<GetAllQuestionsResponse> getUnansweredQuestionsForUser(UUID userId, String category, String difficulty);

    List<GetAllQuestionsHistoryUserResponse> getUserQuestionHistory(UUID userId);

    Map<String, Object> getUserStatistics(UUID userId);

    void deleteQuestion(UUID idQuestion);
}
