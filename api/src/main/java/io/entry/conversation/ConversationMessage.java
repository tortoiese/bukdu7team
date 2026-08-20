package io.entry.conversation;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_message")
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID conversationId;
    private String role;

    @Column(length = 1000)
    private String text;

    private Instant createdAt;

    protected ConversationMessage() {
    }

    private ConversationMessage(UUID conversationId, String role, String text, Instant createdAt) {
        this.conversationId = conversationId;
        this.role = role;
        this.text = text;
        this.createdAt = createdAt;
    }

    public static ConversationMessage user(UUID conversationId, String text, Instant now) {
        return new ConversationMessage(conversationId, "USER", text, now);
    }

    public static ConversationMessage character(UUID conversationId, String text, Instant now) {
        return new ConversationMessage(conversationId, "CHARACTER", text, now);
    }

    public UUID getConversationId() { return conversationId; }
    public String getRole() { return role; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
}
