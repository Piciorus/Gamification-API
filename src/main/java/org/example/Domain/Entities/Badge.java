package org.example.Domain.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Badges")
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private int id;
    @Column(name = "Name", nullable = false, length = 50)
    @Getter @Setter private String name;

    @ManyToMany(mappedBy = "badgesList", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JsonIgnore
    @Getter @Setter private Set<User> users = new HashSet<>();

    public Badge(String name) {
        this.name = name;
    }

    public Badge() {

    }
}
