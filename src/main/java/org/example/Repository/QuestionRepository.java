package org.example.Repository;

import org.example.Domain.Entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    @Query("SELECT q FROM Question q " +
            "WHERE (:category is null OR q.category.name LIKE (:category)) " +
            "AND (:difficulty is null OR q.difficulty IN (:difficulty)) " +
            "ORDER BY q.category.name ASC, q.difficulty ASC")
    List<Question> findAllWithSorting(String category, String difficulty);
    Question getById(final UUID id);
}
