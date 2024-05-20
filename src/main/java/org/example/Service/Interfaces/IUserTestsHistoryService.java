package org.example.Service.Interfaces;

import org.example.Domain.Models.Question.Request.SaveTestHistoryRequest;
import org.example.Domain.Models.Question.Response.GetAllTestsHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface IUserTestsHistoryService {
    void saveTestHistory(UUID userId, SaveTestHistoryRequest request);
    List<GetAllTestsHistoryResponse> getTestHistory(UUID userId);
}
