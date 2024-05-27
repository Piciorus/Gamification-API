package org.example.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum BusinessExceptionCode {
    INVALID_CREDENTIALS("INVALID CREDENTIALS","Invalid username or password"),
    MISSING_COOKIE("MISSING_COOKIE", "Cookie is missing"),
    EXPIRED_REFRESH_TOKEN("EXPIRED_REFRESH_TOKEN", "Session has expired"),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Session is invalid"),
    INVALID_USER_FORMAT("INVALID_USER_FORMAT", "User has an invalid format"),
    USERNAME_ALREADY_REGISTERED("USERNAME_ALREADY_REGISTERED", "User already registered with this username"),
    EMAIL_ALREADY_REGISTERED("EMAIL_ALREADY_REGISTERED","User already registered with this email");

    private String errorId;

    private String message;
}