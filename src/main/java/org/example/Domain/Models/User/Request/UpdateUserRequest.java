package org.example.Domain.Models.User.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.Domain.Entities.Badge;
import org.example.Domain.Entities.ERole;

@Data
@AllArgsConstructor
public class UpdateUserRequest {
    private String username;
    private int threshold;
    private int tokens;
    private ERole role;
}
