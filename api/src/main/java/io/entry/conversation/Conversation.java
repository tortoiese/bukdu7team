package io.entry.conversation;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * P6 대화(최대 3턴). 판매하지 않고 결정 기준 정리에 한정한다(CLAUDE.md, API_CONTRACT.md 7장).
 * turnsUsed는 고객이 보낸 메시지 수를 센다 — 3을 넘으면 더 생성하지 않고 직원 호출로 안내한다.
 */
@Entity
@Table(name = "conversation")
public class Conversation {

    public static final int MAX_TURNS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID sessionId;
    private String scanId;
    private String productId;
    private int turnsUsed;
    private Instant createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "conversation_messages", joinColumns = @JoinColumn(name = "conversation_id"))
    @OrderColumn(name = "idx")
    private List<ConversationMessage> messages = new ArrayList<>();

    protected Conversation() {
    }

    public Conversation(UUID sessionId, String scanId, String productId, Instant createdAt) {
        this.sessionId = sessionId;
        this.scanId = scanId;
        this.productId = productId;
        this.turnsUsed = 0;
        this.createdAt = createdAt;
    }

    public void addMessage(ConversationMessage message) {
        messages.add(message);
    }

    public void useTurn() {
        turnsUsed++;
    }

    public boolean turnsExhausted() {
        return turnsUsed >= MAX_TURNS;
    }

    public int turnsRemaining() {
        return Math.max(0, MAX_TURNS - turnsUsed);
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getScanId() {
        return scanId;
    }

    public String getProductId() {
        return productId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getTurnsUsed() {
        return turnsUsed;
    }

    public List<ConversationMessage> getMessages() {
        return messages;
    }
}
