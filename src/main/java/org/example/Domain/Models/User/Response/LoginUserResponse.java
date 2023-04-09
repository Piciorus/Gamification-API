package org.example.Domain.Models.User.Response;


import java.util.List;

public class LoginUserResponse {
    private String jwttoken;
    private long id;

    private String username;

    private String email;

    private List<String> roles;

    public LoginUserResponse(String accessToken, Long id, String username, String email, List<String> roles) {
        this.jwttoken = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
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
}