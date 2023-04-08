package org.example.Domain.Entities;

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
    @Getter @Setter private int id;

    @Column(name = "Answer", nullable = false, length = 50)
    @Getter @Setter private String answer;

    @Column(name = "Description", nullable = false, length = 200)
    @Getter @Setter private String description;

    @Column(name = "QuestRewardTokens", nullable = false, length = 50)
    @Getter @Setter private int questRewardTokens;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "badge_id")
    @Getter @Setter private Badge badges;

    @ManyToMany(mappedBy = "questsList", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @Getter @Setter private Set<User> users1 = new HashSet<>();

    public Quest(String answer, String description, int questRewardTokens,Badge badges) {
        this.answer = answer;
        this.description = description;
        this.questRewardTokens = questRewardTokens;
        this.badges = badges;
    }

    public Quest() {

    }


}
