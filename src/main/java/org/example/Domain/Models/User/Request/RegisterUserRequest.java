package org.example.Domain.Models.User.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Domain.Entities.ERole;

import javax.validation.constraints.Email;
import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
public class RegisterUserRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private ERole role;
}
