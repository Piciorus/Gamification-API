package org.example.Domain.Models.Leaderboard;

import org.example.Domain.Entities.User;

public class CreateLeaderboardRequest {
    private String points;
    private String position;
    private User user;

    public CreateLeaderboardRequest(String points, String position, User user) {
        this.points = points;
        this.position = position;
        this.user = user;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
