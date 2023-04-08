package org.example.Domain.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;

    @Column(name = "Email", nullable = false, length = 50)
    @Getter @Setter private String Email;
    @Column(name = "Username", nullable = false, length = 50)
    @Getter @Setter private String username;

    @Column(name = "Password", nullable = false, length = 200)
    @Getter @Setter private String password;

    @Column(name = "Tokens", nullable = false, length = 50)
    @Getter @Setter private int tokens;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JsonIgnore
    @JoinTable(
            name = "Quests_Users",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "quest_id")}
    )
    @Getter @Setter private Set<Quest> questsList = new HashSet<>(0);

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JsonIgnore
    @JoinTable(
            name = "User_Badges",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "badge_id")}
    )
    @Getter @Setter private Set<Badge> badgesList = new HashSet<>(0);

    @OneToOne(mappedBy = "user")
    @Getter @Setter private Leaderboard leaderboard;

    public User(String username, String password, int tokens,String email) {
        this.username = username;
        this.password = password;
        this.tokens = tokens;
        this.Email = email;
    }

    public User() {

    }
}
