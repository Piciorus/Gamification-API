package org.example.Domain.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Quests")
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private int id;

    @Column(name = "Answer", nullable = false, length = 50)
    @Getter
    @Setter
    private String answer;

    @Column(name = "Description", nullable = false, length = 200)
    @Getter
    @Setter
    private String description;

    @Column(name = "Rewarded", nullable = true, length = 200)
    @Getter
    @Setter
    private boolean rewarded;
    @Column(name = "Difficulty", nullable = false, length = 50)
    @Getter
    @Setter
    private String difficulty;
    @Column(name = "Threshold", nullable = false, length = 50)
    @Getter
    @Setter
    private int threshold;
    @Column(name = "QuestRewardTokens", nullable = false, length = 50)
    @Getter
    @Setter
    private int questRewardTokens;

    @ManyToMany(mappedBy = "questsList", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JsonIgnore
    @Getter
    @Setter
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
