package org.example.Repository;

import org.example.Domain.Entities.Category;
import org.example.Domain.Entities.UserQuestionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserQuestionHistoryRepository extends JpaRepository<UserQuestionHistory, UUID> {
    @Query("SELECT uqh.question.category.name, COUNT(uqh) FROM UserQuestionHistory uqh WHERE uqh.correct = true GROUP BY uqh.question.category.name")
    List<Object[]> getCountOfCorrectAnswersForEachCategory();
    @Query("SELECT uqh FROM UserQuestionHistory uqh WHERE uqh.user.id = :userId")
    List<UserQuestionHistory> findByUserId(@Param("userId") UUID userId);
}
