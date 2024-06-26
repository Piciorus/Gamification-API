package org.example.Domain.Entities.Security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.example.Domain.Entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

public class UserDetailsImplementation implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final String username;

    private final int tokens;

    private final String email;

    private final int threshold;

    @JsonIgnore
    private final String password;
    @Getter
    private boolean firstLogin;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImplementation(UUID id, String username, String email, String password,
                                     Collection<? extends GrantedAuthority> authorities, int tokens, int threshold, boolean firstLogin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.tokens = tokens;
        this.threshold = threshold;
        this.firstLogin = firstLogin;

    }

    public static UserDetailsImplementation build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImplementation(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getTokens(), user.getThreshold(),user.isFirstLogin());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public UUID getId() {
        return id;
    }

    public int getTokens() {
        return tokens;
    }

    public String getEmail() {
        return email;
    }

    public int getThreshold() {
        return threshold;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImplementation user = (UserDetailsImplementation) o;
        return Objects.equals(id, user.id);
    }
}