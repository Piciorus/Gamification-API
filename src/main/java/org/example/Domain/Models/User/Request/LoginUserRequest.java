package org.example.Domain.Models.User.Request;

import lombok.Getter;
import lombok.Setter;

public class LoginUserRequest {
    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String password;

    public LoginUserRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
