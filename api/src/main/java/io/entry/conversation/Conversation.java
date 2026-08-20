package io.entry.conversation;

import io.entry.common.CharacterId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation", uniqueConstraints = @UniqueConstraint(columnNames = {"sessionId", "scanId"}))
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID sessionId;
    private UUID scanId;
    private String productId;

    @Enumerated(EnumType.STRING)
    private CharacterId character;

    private int turnCount;
    private Instant createdAt;
    private Instant updatedAt;

    protected Conversation() {
    }

    public Conversation(UUID sessionId, UUID scanId, String productId, CharacterId character, Instant createdAt) {
        this.sessionId = sessionId;
        this.scanId = scanId;
        this.productId = productId;
        this.character = character;
        this.turnCount = 0;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public void increaseTurn(Instant now) {
        turnCount++;
        updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getSessionId() { return sessionId; }
    public UUID getScanId() { return scanId; }
    public String getProductId() { return productId; }
    public CharacterId getCharacter() { return character; }
    public int getTurnCount() { return turnCount; }
}
