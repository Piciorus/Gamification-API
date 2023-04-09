package org.example.Domain.Models.Quest;

import org.example.Domain.Entities.Badge;

public class CreateQuestRequest {
    private String answer;
    private String description;
    private int questRewardTokens;
    private Badge badge;

    public CreateQuestRequest(String answer,String description, int reward, Badge badge) {
        this.answer = answer;
        this.description = description;
        this.questRewardTokens = reward;
        this.badge = badge;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getReward() {
        return questRewardTokens;
    }

    public void setReward(int reward) {
        this.questRewardTokens = reward;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String name) {
        this.answer = name;
    }

    public Badge getBadge() {
        return badge;
    }

    public void setBadge(Badge badge) {
        this.badge = badge;
    }
}
