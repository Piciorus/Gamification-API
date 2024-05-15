package org.example.Domain.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
public class Question extends BaseEntity {
    @Column(name = "question_text", nullable = false)
    private String questionText;
    @Column(name = "answer1")
    private String answer1;
    @Column(name = "answer2")
    private String answer2;
    @Column(name = "answer3")
    private String answer3;
    @Column(name = "correct_answer")
    private String correctAnswer;
    @Column(name = "rewarded", nullable = true, length = 200)
    private boolean rewarded;
    @Column(name = "difficulty", nullable = false, length = 50)
    private String difficulty;
    @Column(name = "threshold", nullable = false, length = 50)
    private int threshold;
    @Column(name = "questRewardTokens", nullable = false, length = 50)
    private int questRewardTokens;
    @Column(name = "checked_by_admin", nullable = true, length = 200)
    private boolean checkByAdmin;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    @ManyToMany(mappedBy = "answeredQuestions", fetch = FetchType.LAZY)
    private Set<User> answeredByUsers = new HashSet<>();
}
