package org.example.Domain.Models.Quest.Request;

import org.example.Domain.Entities.Badge;

public class CreateQuestRequest {
    private String answer;
    private String description;
    private int questRewardTokens;

    private String difficulty;

    private int threshold;

    private int rewardTokens;


    public CreateQuestRequest(String answer,String description, int questRewardTokens, String difficulty, int threshold, int rewardTokens) {
        this.answer = answer;
        this.description = description;
        this.questRewardTokens = questRewardTokens;
        this.difficulty = difficulty;
        this.threshold = threshold;
        this.rewardTokens = rewardTokens;
    }

    public CreateQuestRequest() {
    }

    public int getRewardTokens(){
        return rewardTokens;
    }

    public void setRewardTokens(int rewardTokens){
        this.rewardTokens = rewardTokens;
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

    public void setReward(int questRewardTokens) {
        this.questRewardTokens = questRewardTokens;
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
