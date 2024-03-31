package org.example.Service.Implementation;

import org.example.Domain.Entities.Question;
import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.Question.Request.CreateQuestionRequest;
import org.example.Domain.Models.Question.Response.GetAllQuestionsResponse;
import org.example.Domain.Models.Question.Request.UpdateQuestionRequest;
import org.example.Repository.QuestionRepository;
import org.example.Service.Interfaces.IQuestionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class QuestionService implements IQuestionService {
    private final QuestionRepository questionRepository;
    private final Mapper mapper;


    public QuestionService(QuestionRepository questionRepository, Mapper mapper) {
        this.questionRepository = questionRepository;
        this.mapper=mapper;
    }

    @Override
    public List<GetAllQuestionsResponse> getQuestionsForTest(){
        List<GetAllQuestionsResponse> list = new ArrayList<>();
        questionRepository.findAllRandomQuestions().forEach(quest -> list.add(mapper.QuestionToGetAllQuestionsResponse(quest)));
        return list;
    }

    @Override
    public Question createQuestion(CreateQuestionRequest createQuestionRequest) {
        Question quest = mapper.CreateQuestionRequestToQuestion(createQuestionRequest);
        return questionRepository.save(quest);
    }

    @Override
    public Question updateQuestion(UpdateQuestionRequest updateQuestionRequest, UUID idQuestion) {
        Question questFromDb = questionRepository.getById(idQuestion);
        mapper.UpdateQuestionRequestToQuestion(updateQuestionRequest, questFromDb);
        return questionRepository.save(questFromDb);
    }
}
