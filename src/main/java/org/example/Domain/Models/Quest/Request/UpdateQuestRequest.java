package org.example.Domain.Models.Quest.Request;

import lombok.Getter;
import lombok.Setter;

public class UpdateQuestRequest {
    @Getter @Setter private String description;
    @Getter @Setter private String answer;
    @Getter @Setter private int threshold;
    @Getter @Setter private int questRewardTokens;
    @Getter @Setter private boolean rewarded;

    public UpdateQuestRequest(String description, String answer, int threshold, int questRewardTokens, boolean rewarded) {
        this.description = description;
        this.answer = answer;
        this.threshold = threshold;
        this.questRewardTokens = questRewardTokens;
        this.rewarded = rewarded;
    }

    public UpdateQuestRequest() {
    }
}
