package org.example.Domain.Models.User.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class LoginUserResponse {
    private String jwttoken;
    private UUID id;
    private String username;
    private int tokens;
    private String email;
    private final List<String> roles;
    private int threshold;

    public LoginUserResponse(String accessToken, UUID id, String username, String email, List<String> roles, int tokens, int threshold) {
        this.jwttoken = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.tokens = tokens;
        this.threshold = threshold;
    }
}