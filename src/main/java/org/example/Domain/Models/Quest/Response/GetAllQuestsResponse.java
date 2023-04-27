package org.example.Domain.Models.Quest.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class GetAllQuestsResponse {
    @Getter
    @Setter
    private UUID id;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String answer;
    @Getter
    @Setter
    private boolean rewarded;
    @Getter
    @Setter
    private String difficulty;
    @Getter
    @Setter
    private int threshold;
    @Getter
    @Setter
    private int questRewardTokens;

    public GetAllQuestsResponse(UUID id, String description, String answer, boolean rewarded, String difficulty, int threshold, int questRewardTokens) {
        this.id = id;
        this.description = description;
        this.answer = answer;
        this.rewarded = rewarded;
        this.difficulty = difficulty;
        this.threshold = threshold;
        this.questRewardTokens = questRewardTokens;
    }

    public GetAllQuestsResponse() {
    }

}
