package org.example.Domain.Models.Question.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Domain.Entities.Category;
import org.example.Domain.Models.Category.GetAllCategoriesResponse;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveTestHistoryRequest {
    private LocalDateTime testDate;
    private int chatGptCorrectAnswers;
    private int userCorrectAnswers;
    private int questionsAnswered;
    private GetAllCategoriesResponse category;
}
