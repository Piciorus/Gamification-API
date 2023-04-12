package org.example.Domain.Models.User.Request;

public class LoginUserRequest {
    private static String username;
    private static String password;

    public LoginUserRequest(String username, String password) {
        LoginUserRequest.username = username;
        LoginUserRequest.password = password;
    }

    public static String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        LoginUserRequest.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        LoginUserRequest.password = password;
    }
}
