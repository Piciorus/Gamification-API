package org.example.Domain.Models.Question;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Domain.Entities.Category;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetAllQuestionsResponse {
    private String questionText;
    private String answer1;
    private String answer2;
    private String answer3;
    private String correctAnswer;
    private boolean rewarded;
    private String difficulty;
    private int threshold;
    private int questRewardTokens;
    private boolean checkByAdmin;
    private Category category;
}
