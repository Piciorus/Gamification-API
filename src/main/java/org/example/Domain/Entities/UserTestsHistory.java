package org.example.Domain.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_tests_history")
@Getter
@Setter
@NoArgsConstructor
public class UserTestsHistory extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "test_date", nullable = false)
    private LocalDateTime testDate;
    @Column(name = "chatgpt_correct_answers", nullable = false)
    private int chatGptCorrectAnswers;
    @Column(name = "user_correct_answers", nullable = false)
    private int userCorrectAnswers;
    @Column(name = "questions_answered", nullable = false)
    private int questionsAnswered;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
