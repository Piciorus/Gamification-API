package org.example.Service.Interfaces;

import org.example.Domain.Entities.Quest;
import org.example.Domain.Entities.Question;
import org.example.Domain.Models.Question.GetAllQuestionsResponse;

import java.util.List;

public interface IQuestionService {
    List<GetAllQuestionsResponse> getOneQuestionForTest();
}
