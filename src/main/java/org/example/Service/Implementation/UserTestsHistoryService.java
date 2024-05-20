package org.example.Service.Implementation;

import org.example.Domain.Entities.User;
import org.example.Domain.Entities.UserTestsHistory;
import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.Question.Request.SaveTestHistoryRequest;
import org.example.Domain.Models.Question.Response.GetAllTestsHistoryResponse;
import org.example.Repository.UserTestsHistoryRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.IUserTestsHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserTestsHistoryService implements IUserTestsHistoryService {
    private final Mapper mapper;
    private final UsersRepository usersRepository;
    private final UserTestsHistoryRepository userTestsHistoryRepository;

    public UserTestsHistoryService(Mapper mapper, UsersRepository usersRepository, UserTestsHistoryRepository userTestsHistoryRepository) {
        this.mapper = mapper;
        this.usersRepository = usersRepository;
        this.userTestsHistoryRepository = userTestsHistoryRepository;
    }

    @Override
    public void saveTestHistory(UUID userId, SaveTestHistoryRequest request) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        UserTestsHistory userTestsHistory = mapper.saveTestHistoryRequestToUserTestsHistory(request);
        userTestsHistory.setUser(user);

        userTestsHistoryRepository.save(userTestsHistory);
    }


    @Override
    public List<GetAllTestsHistoryResponse> getTestHistory(UUID userId) {
        List<UserTestsHistory> userTestsHistories = userTestsHistoryRepository.findAllByUserId(userId);
        return userTestsHistories.stream()
                .map(mapper::userTestsHistoryToResponse)
                .collect(Collectors.toList());
    }
}
