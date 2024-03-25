package org.example.Controller;

import org.example.Domain.Entities.Quest;
import org.example.Domain.Entities.Question;
import org.example.Domain.Models.Question.GetAllQuestionsResponse;
import org.example.Service.Interfaces.IQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/test")
public class QuestionController {
    private final IQuestionService questionService;

    @Autowired
    public QuestionController(IQuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping(path = "/question")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<GetAllQuestionsResponse>> getQuestionForTest() {
        return ResponseEntity.ok(questionService.getOneQuestionForTest());
    }

}
