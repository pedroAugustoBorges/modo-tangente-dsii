package br.edu.fatec.modotangente.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "question_options", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"question_id", "position"}),
        @UniqueConstraint(columnNames = {"id", "question_id"})
})
public class QuizQuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private QuizQuestion question;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "option_text", nullable = false, columnDefinition = "text")
    private String optionText;

    @Column(name = "is_correct", nullable = false)
    private Boolean correct = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public QuizQuestion getQuestion() { return question; }
    public void setQuestion(QuizQuestion question) { this.question = question; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    public Boolean getCorrect() { return correct; }
    public void setCorrect(Boolean correct) { this.correct = correct; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void setCreationTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
    }
}
