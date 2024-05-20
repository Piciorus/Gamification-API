package org.example.Service.Implementation;

import jakarta.persistence.EntityNotFoundException;
import org.example.Domain.Entities.Category;
import org.example.Domain.Entities.Question;
import org.example.Domain.Entities.User;
import org.example.Domain.Entities.UserQuestionHistory;
import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.Question.Request.CreateQuestionRequest;
import org.example.Domain.Models.Question.Request.UpdateQuestionRequest;
import org.example.Domain.Models.Question.Request.UserAnswerRequest;
import org.example.Domain.Models.Question.Response.GetAllQuestionsHistoryUserResponse;
import org.example.Domain.Models.Question.Response.GetAllQuestionsResponse;
import org.example.Repository.CategoryRepository;
import org.example.Repository.QuestionRepository;
import org.example.Repository.UserQuestionHistoryRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.IQuestionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionService implements IQuestionService {
    private final QuestionRepository questionRepository;
    private final Mapper mapper;
    private final UsersRepository usersRepository;
    private final UserQuestionHistoryRepository userQuestionHistoryRepository;
    private final CategoryRepository categoryRepository;

    public QuestionService(QuestionRepository questionRepository, Mapper mapper, UsersRepository usersRepository, UserQuestionHistoryRepository userQuestionHistoryRepository, CategoryRepository categoryRepository) {
        this.questionRepository = questionRepository;
        this.mapper = mapper;
        this.usersRepository = usersRepository;
        this.userQuestionHistoryRepository = userQuestionHistoryRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<GetAllQuestionsResponse> getQuestionsForTest(String category, String difficulty) {
        List<GetAllQuestionsResponse> list = new ArrayList<>();
        questionRepository.findAllWithSorting(category, difficulty).forEach(quest -> list.add(mapper.QuestionToGetAllQuestionsResponse(quest)));
        return list;
    }

    @Override
    public Question createQuestion(CreateQuestionRequest createQuestionRequest) {
        Category category = categoryRepository.findByName(createQuestionRequest.getCategory());
        Question quest = mapper.CreateQuestionRequestToQuestion(createQuestionRequest);
        quest.setCategory(category);
        return questionRepository.save(quest);
    }

    @Override
    public Question updateQuestion(UpdateQuestionRequest updateQuestionRequest, UUID idQuestion) {
        Category category = categoryRepository.findByName(updateQuestionRequest.getCategory());
        Question questFromDb = questionRepository.getById(idQuestion);
        mapper.UpdateQuestionRequestToQuestion(updateQuestionRequest, questFromDb);
        questFromDb.setCategory(category);
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

    @Override
    public Map<String, Long> getCountOfCorrectAnswersForEachCategory() {
        List<Object[]> results = userQuestionHistoryRepository.getCountOfCorrectAnswersForEachCategory();
        Map<String, Long> counts = new HashMap<>();

        for (Object[] result : results) {
            String categoryName = (String) result[0];
            Long count = (Long) result[1];
            counts.put(categoryName, count);
        }

        return counts;
    }

    @Override
    public List<GetAllQuestionsResponse> getUnansweredQuestionsForUser(UUID userId, String category, String difficulty) {
        List<GetAllQuestionsResponse> list = new ArrayList<>();
        questionRepository.findAllUnansweredByUserWithSorting(userId, category, difficulty).forEach(quest -> list.add(mapper.QuestionToGetAllQuestionsResponse(quest)));
        return list;
    }

    @Override
    public void markAnswerAsCorrect(UUID userId, UUID questionId, UserAnswerRequest userAnswer) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));

        UserQuestionHistory userQuestionHistory = new UserQuestionHistory();
        userQuestionHistory.setUser(user);
        userQuestionHistory.setQuestion(question);
        userQuestionHistory.setAnswerDate(LocalDateTime.now());
        userQuestionHistory.setCorrect(Objects.equals(userAnswer.getUserAnswer(), question.getCorrectAnswer()));
        userQuestionHistory.setUserAnswer(userAnswer.getUserAnswer());

        userQuestionHistoryRepository.save(userQuestionHistory);
    }

    @Override
    public List<GetAllQuestionsHistoryUserResponse> getUserQuestionHistory(UUID userId) {
        List<UserQuestionHistory> userQuestionHistories = userQuestionHistoryRepository.findByUserId(userId);
        return userQuestionHistories.stream()
                .map(mapper::userQuestionHistoryToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getUserStatistics(UUID userId) {
        List<UserQuestionHistory> userQuestionHistories = userQuestionHistoryRepository.findByUserId(userId);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalQuestionsAnswered", userQuestionHistories.size());

        long correctAnswers = userQuestionHistories.stream()
                .filter(UserQuestionHistory::isCorrect)
                .count();
        statistics.put("correctAnswers", correctAnswers);

        long incorrectAnswers = userQuestionHistories.size() - correctAnswers;
        statistics.put("incorrectAnswers", incorrectAnswers);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);

        long questionsAnsweredLast7Days = userQuestionHistories.stream()
                .filter(uqh -> uqh.getAnswerDate().isAfter(sevenDaysAgo))
                .count();
        statistics.put("questionsAnsweredLast7Days", questionsAnsweredLast7Days);

        return statistics;
    }

    @Override
    public void deleteQuestion(UUID idQuestion) {
        questionRepository.deleteById(idQuestion);
    }
}
