package org.example.Domain.Models.User.Response;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public class LoginUserResponse {
    @Getter
    @Setter
    private String jwttoken;
    @Getter
    @Setter
    private UUID id;
    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private int tokens;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private final List<String> roles;
    @Getter
    @Setter
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