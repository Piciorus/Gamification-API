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
    private int threshold;

    public LoginUserResponse(String jwttoken, UUID id, String username, String email, int tokens, int threshold) {
        this.jwttoken = jwttoken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.tokens = tokens;
        this.threshold = threshold;
    }

//    public LoginUserResponse(String username, String email, List<String> roles, int tokens, int threshold) {
//        this.username = username;
//        this.email = email;
//        this.roles = roles;
//        this.tokens = tokens;
//        this.threshold = threshold;
//    }
}