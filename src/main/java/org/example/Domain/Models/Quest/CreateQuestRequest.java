package org.example.Domain.Models.Quest;

import org.example.Domain.Entities.Badge;

public class CreateQuestRequest {
    private String answer;
    private String description;
    private int questRewardTokens;

    private String difficulty;

    private int threshold;


    public CreateQuestRequest(String answer,String description, int reward, String difficulty, int threshold) {
        this.answer = answer;
        this.description = description;
        this.questRewardTokens = reward;
        this.difficulty = difficulty;
        this.threshold = threshold;
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

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
