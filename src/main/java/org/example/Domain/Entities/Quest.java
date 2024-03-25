package org.example.Domain.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "quests")
public class Quest extends BaseEntity {
    @Column(name = "answer", nullable = false, length = 50)
    private String answer;
    @Column(name = "description", nullable = false, length = 200)
    private String description;
    @Column(name = "rewarded", nullable = true, length = 200)
    private boolean rewarded;
    @Column(name = "difficulty", nullable = false, length = 50)
    private String difficulty;
    @Column(name = "threshold", nullable = false, length = 50)
    private int threshold;
    @Column(name = "questRewardTokens", nullable = false, length = 50)
    private int questRewardTokens;

    @ManyToMany(mappedBy = "questsList", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<User> users1 = new HashSet<>();

    public Quest(String answer, String description, int questRewardTokens, String difficulty, int threshold, boolean rewarded) {
        this.answer = answer;
        this.description = description;
        this.questRewardTokens = questRewardTokens;
        this.difficulty = difficulty;
        this.threshold = threshold;
        this.rewarded = rewarded;
    }

    public Quest() {

    }


}
