package org.example.Domain.Models.User.Response;

import lombok.Getter;
import lombok.Setter;
import org.example.Domain.Entities.Role;

import java.util.Set;

public class GetUserByIdResponse {
    @Getter @Setter private int id;
    @Getter @Setter private String username;
    @Getter @Setter private int threshold;
    @Getter @Setter private int tokens;
    @Getter @Setter private String email;
    @Getter @Setter private Set<Role> roles;

    public GetUserByIdResponse(String username, int threshold, int tokens, String email, Set<Role> roles, int id) {
        this.username = username;
        this.threshold = threshold;
        this.tokens = tokens;
        this.email = email;
        this.roles = roles;
        this.id = id;
    }

    public GetUserByIdResponse() {
    }


}
