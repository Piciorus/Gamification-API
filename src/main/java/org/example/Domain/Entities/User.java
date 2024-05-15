package org.example.Domain.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
    @Column(name = "avatar", nullable = false, length = 50)
    private String avatar;
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_questions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id"))
    private Set<Question> answeredQuestions = new HashSet<>();

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
