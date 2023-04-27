package org.example.Domain.Models.Quest.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class CheckAnswerRequest {
    @Getter
    @Setter
    UUID userId;
    @Getter
    @Setter
    String answer;
    @Getter
    @Setter
    UUID questId;

    public CheckAnswerRequest(UUID userId, String answer, UUID questId) {
        this.userId = userId;
        this.answer = answer;
        this.questId = questId;
    }

    public CheckAnswerRequest() {
    }


}
