package org.example.Service.Interfaces;

import org.example.Domain.Entities.Question;
import org.example.Domain.Models.Question.Request.CreateQuestionRequest;
import org.example.Domain.Models.Question.Response.GetAllQuestionsResponse;
import org.example.Domain.Models.Question.Request.UpdateQuestionRequest;

import java.util.List;
import java.util.UUID;

public interface IQuestionService {

    /**
     * Retrieves a list of questions for a test.
     *
     * @return A list of questions for the test.
     */
    List<GetAllQuestionsResponse> getQuestionsForTest();

    Question createQuestion(CreateQuestionRequest createQuestionRequest);

    Question updateQuestion(UpdateQuestionRequest updateQuestionRequest, UUID idQuestion);

}
