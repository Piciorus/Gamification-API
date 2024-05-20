package org.example.Domain.Models.Question.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetAllQuestionsHistoryUserResponse {
    private UUID id;
    private Date creationDate;
    private Date updateDate;
    private GetAllQuestionsResponse question;
    private LocalDateTime answerDate;
    private boolean correct;
    private String userAnswer;
}
