package br.edu.fatec.modotangente.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "question_attempts")
public class QuestionAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "quiz_attempt_id", nullable = false)
    private Long quizAttemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "quiz_attempt_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "quiz_id", referencedColumnName = "quiz_id", insertable = false, updatable = false)
    })
    private QuizAttempt quizAttempt;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "question_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "quiz_id", referencedColumnName = "quiz_id", insertable = false, updatable = false)
    })
    private QuizQuestion question;

    @Column(name = "selected_option_id")
    private Long selectedOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "selected_option_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "question_id", referencedColumnName = "question_id", insertable = false, updatable = false)
    })
    private QuizQuestionOption selectedOption;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "answered_at")
    private Instant answeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQuizAttemptId() { return quizAttemptId; }
    public void setQuizAttemptId(Long quizAttemptId) { this.quizAttemptId = quizAttemptId; }
    public QuizAttempt getQuizAttempt() { return quizAttempt; }
    public void setQuizAttempt(QuizAttempt quizAttempt) { this.quizAttempt = quizAttempt; }
    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public QuizQuestion getQuestion() { return question; }
    public void setQuestion(QuizQuestion question) { this.question = question; }
    public Long getSelectedOptionId() { return selectedOptionId; }
    public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
    public QuizQuestionOption getSelectedOption() { return selectedOption; }
    public void setSelectedOption(QuizQuestionOption selectedOption) { this.selectedOption = selectedOption; }
    public Boolean getCorrect() { return correct; }
    public void setCorrect(Boolean correct) { this.correct = correct; }
    public Instant getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Instant answeredAt) { this.answeredAt = answeredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void setCreationTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
    }
}
