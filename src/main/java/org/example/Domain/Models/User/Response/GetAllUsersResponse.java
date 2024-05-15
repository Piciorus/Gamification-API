package org.example.Domain.Models.User.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Domain.Entities.Badge;
import org.example.Domain.Entities.Role;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetAllUsersResponse {
    private UUID id;
    private String username;
    private int threshold;
    private int tokens;
    private String email;
    private Set<Role> roles;
    private Set<Badge> badges;
    private String avatar;
}
