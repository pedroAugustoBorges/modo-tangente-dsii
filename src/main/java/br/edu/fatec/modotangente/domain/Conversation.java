package br.edu.fatec.modotangente.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "question_attempt_id", nullable = false, unique = true)
    private Long questionAttemptId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_attempt_id", insertable = false, updatable = false)
    private QuestionAttempt questionAttempt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQuestionAttemptId() { return questionAttemptId; }
    public void setQuestionAttemptId(Long questionAttemptId) { this.questionAttemptId = questionAttemptId; }
    public QuestionAttempt getQuestionAttempt() { return questionAttempt; }
    public void setQuestionAttempt(QuestionAttempt questionAttempt) { this.questionAttempt = questionAttempt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void setCreationTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void touchUpdatedAt() { updatedAt = Instant.now(); }
}
