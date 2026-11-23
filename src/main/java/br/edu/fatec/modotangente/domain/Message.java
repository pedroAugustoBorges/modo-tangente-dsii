package br.edu.fatec.modotangente.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", insertable = false, updatable = false)
    private Conversation conversation;

    @Column(name = "sender_role", nullable = false, length = 20)
    private String senderRole;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "reply_to_message_id")
    private Long replyToMessageId;

    @Column(name = "llm_status", length = 20)
    private String llmStatus;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }
    public Long getReplyToMessageId() { return replyToMessageId; }
    public void setReplyToMessageId(Long replyToMessageId) { this.replyToMessageId = replyToMessageId; }
    public String getLlmStatus() { return llmStatus; }
    public void setLlmStatus(String llmStatus) { this.llmStatus = llmStatus; }
    public Instant getProcessingStartedAt() { return processingStartedAt; }
    public void setProcessingStartedAt(Instant processingStartedAt) { this.processingStartedAt = processingStartedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void setCreationTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
    }
}
