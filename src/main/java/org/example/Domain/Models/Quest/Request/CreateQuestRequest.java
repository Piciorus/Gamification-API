package org.example.Domain.Models.Quest.Request;

import lombok.Getter;
import lombok.Setter;

public class CreateQuestRequest {
    @Getter
    @Setter
    private String answer;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private int questRewardTokens;
    @Getter
    @Setter
    private String difficulty;
    @Getter
    @Setter
    private int threshold;
    @Getter
    @Setter
    private int rewardTokens;

    public CreateQuestRequest(String answer, String description, int questRewardTokens, String difficulty, int threshold, int rewardTokens) {
        this.answer = answer;
        this.description = description;
        this.questRewardTokens = questRewardTokens;
        this.difficulty = difficulty;
        this.threshold = threshold;
        this.rewardTokens = rewardTokens;
    }

    public CreateQuestRequest() {
    }


}
