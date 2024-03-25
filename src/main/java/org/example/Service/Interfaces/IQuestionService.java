package org.example.Service.Interfaces;

import org.example.Domain.Models.Question.GetAllQuestionsResponse;

import java.util.List;

public interface IQuestionService {

    /**
     * Retrieves a list of questions for a test.
     *
     * @return A list of questions for the test.
     */
    List<GetAllQuestionsResponse> getQuestionsForTest();
}
