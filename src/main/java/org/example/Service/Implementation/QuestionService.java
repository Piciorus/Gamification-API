package org.example.Service.Implementation;

import org.example.Domain.Entities.Question;
import org.example.Domain.Entities.User;
import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.Question.Request.CreateQuestionRequest;
import org.example.Domain.Models.Question.Request.UpdateQuestionRequest;
import org.example.Domain.Models.Question.Response.GetAllQuestionsResponse;
import org.example.Repository.QuestionRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.IQuestionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class QuestionService implements IQuestionService {
    private final QuestionRepository questionRepository;
    private final Mapper mapper;
    private final UsersRepository usersRepository;

    public QuestionService(QuestionRepository questionRepository, Mapper mapper, UsersRepository usersRepository) {
        this.questionRepository = questionRepository;
        this.mapper = mapper;
        this.usersRepository = usersRepository;
    }

    @Override
    public List<GetAllQuestionsResponse> getQuestionsForTest(String category, String difficulty) {
        List<GetAllQuestionsResponse> list = new ArrayList<>();
        questionRepository.findAllWithSorting(category, difficulty).forEach(quest -> list.add(mapper.QuestionToGetAllQuestionsResponse(quest)));
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

    @Override
    public void resolveQuestion(UUID idQuest, UUID idUser) {
        Question quest = questionRepository.getById(idQuest);
        User user = usersRepository.getById(idUser);
        user.setThreshold(user.getThreshold() + quest.getThreshold());
        user.setTokens(user.getTokens() + quest.getQuestRewardTokens());
        usersRepository.save(user);
    }
}
