package org.example.Domain.Models.User.Response;


import java.util.List;

public class LoginUserResponse {
    private String jwttoken;
    private long id;

    private String username;

    private int tokens;
    private String email;

    private List<String> roles;

    private int threshold;


    public LoginUserResponse(String accessToken, Long id, String username, String email, List<String> roles, int tokens, int threshold) {
        this.jwttoken = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.tokens = tokens;
        this.threshold = threshold;
    }

    public String getAccessToken() {
        return jwttoken;
    }

    public void setAccessToken(String accessToken) {
        this.jwttoken = accessToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}