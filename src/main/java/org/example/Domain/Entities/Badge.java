package org.example.Domain.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "badges")
public class Badge extends BaseEntity {
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToMany(mappedBy = "badgesList", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    public Badge(String name) {
        this.name = name;
    }

    public Badge() {

    }
}
