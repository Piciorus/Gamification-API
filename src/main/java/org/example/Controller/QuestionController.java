package org.example.Controller;

import org.example.Domain.Models.Question.Request.CreateQuestionRequest;
import org.example.Domain.Models.Question.Response.GetAllQuestionsResponse;
import org.example.Domain.Models.Question.Request.UpdateQuestionRequest;
import org.example.Service.Interfaces.IQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;


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
        return ResponseEntity.ok(questionService.getQuestionsForTest());
    }

    @PostMapping(path = "/createQuestion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity createQuestion(@Valid @RequestBody final CreateQuestionRequest createQuestionRequest) {
        questionService.createQuestion(createQuestionRequest);
        return ResponseEntity.ok().body("Question created!");
    }

    @PutMapping(path = "/updateQuestion/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity updateQuestion(@Valid @RequestBody final UpdateQuestionRequest updateQuestionRequest, @PathVariable("id") @NotBlank UUID idQuestion) {
        questionService.updateQuestion(updateQuestionRequest, idQuestion);
        return ResponseEntity.ok().body("Question updated!");
    }

}
