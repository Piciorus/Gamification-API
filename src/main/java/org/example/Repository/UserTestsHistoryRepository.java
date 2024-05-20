package org.example.Repository;

import org.example.Domain.Entities.UserTestsHistory;
import org.example.Domain.Models.Question.Request.SaveTestHistoryRequest;
import org.example.Domain.Models.Question.Response.GetAllTestsHistoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserTestsHistoryRepository extends JpaRepository<UserTestsHistory, UUID> {
    List<UserTestsHistory> findAllByUserId(UUID userId);
}
