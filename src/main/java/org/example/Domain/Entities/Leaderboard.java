package org.example.Domain.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "Leaderboard")
public class Leaderboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;
    @Column(name = "Points", nullable = false, length = 50)
    @Getter @Setter private String points;
    @Column(name = "Position", nullable = false, length = 200)
    @Getter @Setter private String position;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @Getter @Setter private User user;

    public Leaderboard(String position, String points,User user) {
        this.user = user;
        this.points = points;
        this.position = position;
    }

    public Leaderboard() {

    }
}
