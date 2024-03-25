package org.example.Service.Implementation;

import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.Question.GetAllQuestionsResponse;
import org.example.Repository.QuestionRepository;
import org.example.Service.Interfaces.IQuestionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService implements IQuestionService {
    private final QuestionRepository questionRepository;
    private final Mapper mapper;


    public QuestionService(QuestionRepository questionRepository, Mapper mapper) {
        this.questionRepository = questionRepository;
        this.mapper=mapper;
    }

    @Override
    public List<GetAllQuestionsResponse> getOneQuestionForTest(){
        List<GetAllQuestionsResponse> list = new ArrayList<>();
        questionRepository.findAllRandomQuestions().forEach(quest -> list.add(mapper.QuestionToGetAllQuestionsResponse(quest)));
        return list;
    }
}
