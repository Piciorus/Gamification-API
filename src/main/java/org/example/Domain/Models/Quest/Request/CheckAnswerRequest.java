package org.example.Domain.Models.Quest.Request;

import lombok.Getter;
import lombok.Setter;

public class CheckAnswerRequest {
    @Getter
    @Setter
    int userId;
    @Getter
    @Setter
    String answer;
    @Getter
    @Setter
    int questId;

    public CheckAnswerRequest(int userId, String answer, int questId) {
        this.userId = userId;
        this.answer = answer;
        this.questId = questId;
    }

    public CheckAnswerRequest() {
    }


}
