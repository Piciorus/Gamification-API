package org.example.Domain.Models.Question.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Domain.Entities.Category;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetAllTestsHistoryResponse {
    private LocalDateTime testDate;
    private int chatGptCorrectAnswers;
    private int userCorrectAnswers;
    private int questionsAnswered;
    private Category category;
}
