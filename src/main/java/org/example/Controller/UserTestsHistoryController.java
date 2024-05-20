package org.example.Controller;

import org.example.Domain.Models.Question.Request.SaveTestHistoryRequest;
import org.example.Domain.Models.Question.Response.GetAllTestsHistoryResponse;
import org.example.Service.Interfaces.IUserTestsHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/history")
public class UserTestsHistoryController {
    private final IUserTestsHistoryService userTestsHistoryService;

    @Autowired
    public UserTestsHistoryController(IUserTestsHistoryService userTestsHistoryService) {
        this.userTestsHistoryService = userTestsHistoryService;
    }

    @PostMapping("/saveHistoryTest/{userId}")
    public ResponseEntity<Void> saveTestHistory(@RequestBody SaveTestHistoryRequest request, @PathVariable("userId") UUID userId) {
        userTestsHistoryService.saveTestHistory(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/getHistoryTest/{userId}")
    public ResponseEntity<List<GetAllTestsHistoryResponse>> getTestHistory(@PathVariable("userId") UUID userId) {
        List<GetAllTestsHistoryResponse> testHistory = userTestsHistoryService.getTestHistory(userId);
        return ResponseEntity.ok(testHistory);
    }
}
