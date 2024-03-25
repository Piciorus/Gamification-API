package org.example.Domain.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {
    @Column(name = "Email", nullable = false, length = 50)
    private String Email;
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 200)
    private String password;

    @Column(name = "threshold", nullable = true, length = 200)
    private int threshold;
    @Column(name = "tokens", nullable = false, length = 50)
    private int tokens;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JsonIgnore
    @JoinTable(
            name = "quests_Users",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "quest_id")}
    )
    private Set<Quest> questsList = new HashSet<>(0);

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JsonIgnore
    @JoinTable(
            name = "user_Badges",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "badge_id")}
    )
    private Set<Badge> badgesList = new HashSet<>(0);

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(String username, String password, int tokens, String email, int threshold) {
        this.username = username;
        this.Email = email;
        this.tokens = tokens;
        this.password = password;
        this.threshold = threshold;
    }

    public User() {

    }
}
