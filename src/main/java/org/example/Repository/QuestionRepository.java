package org.example.Repository;

import org.example.Domain.Entities.Category;
import org.example.Domain.Entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    @Query("SELECT q FROM Question q " +
            "WHERE (:category is null OR q.category.name LIKE (:category)) " +
            "AND (:difficulty is null OR q.difficulty IN (:difficulty)) " +
            "ORDER BY q.category.name ASC, q.difficulty ASC")
    List<Question> findAllWithSorting(String category, String difficulty);
    Question getById(final UUID id);

    @Query("SELECT q FROM Question q " +
            "LEFT JOIN UserQuestionHistory uqh ON q.id = uqh.question.id AND uqh.user.id = :userId " +
            "WHERE (:category is null OR q.category.name LIKE (:category)) " +
            "AND (:difficulty is null OR q.difficulty IN (:difficulty)) " +
            "AND (uqh IS NULL OR uqh.correct = false) " +
            "ORDER BY q.category.name ASC, q.difficulty ASC")
    List<Question> findAllUnansweredByUserWithSorting(@Param("userId") UUID userId, @Param("category") String category, @Param("difficulty") String difficulty);

}