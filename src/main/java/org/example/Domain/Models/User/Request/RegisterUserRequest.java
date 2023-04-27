package org.example.Domain.Models.User.Request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import java.util.Date;
import java.util.Set;

public class RegisterUserRequest {
    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    @Email
    private String email;
    @Getter
    @Setter
    private Date creationDate;
    @Getter
    @Setter
    private Set<String> roles;

    public RegisterUserRequest(String username, String password, String email, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
        this.creationDate = new Date();
    }

}
