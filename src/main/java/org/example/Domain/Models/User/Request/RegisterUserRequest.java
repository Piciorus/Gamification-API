package org.example.Domain.Models.User.Request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class RegisterUserRequest {
    private String username;
    private String password;
    @Email
    private String email;
    private Date creationDate;
    private Set<String> roles;

    public RegisterUserRequest(String username, String password, String email, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
        this.creationDate = new Date();
    }
}
