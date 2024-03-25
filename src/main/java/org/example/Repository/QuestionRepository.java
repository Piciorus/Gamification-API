package org.example.Repository;

import org.example.Domain.Entities.Question;
import org.example.Domain.Models.Question.GetAllQuestionsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    @Query(value = "SELECT q FROM Question q")
    List<Question> findAllRandomQuestions();
}
