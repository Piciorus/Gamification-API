package org.example.Domain.Models.User.Request;

public class LoginUserRequest {
    private static String username;
    private static String password;

    public LoginUserRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
