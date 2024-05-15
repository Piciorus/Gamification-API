package org.example.Domain.Models.Question.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateQuestionRequest {
    private String questionText;
    private String answer1;
    private String answer2;
    private String answer3;
    private String correctAnswer;
    private String difficulty;
    private int threshold;
    private int questRewardTokens;
}
