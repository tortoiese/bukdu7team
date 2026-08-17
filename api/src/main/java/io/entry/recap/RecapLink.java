package io.entry.recap;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * 계정 연결 기록. 원문 연락처는 저장하지 않고 SHA-256 해시만 보관한다(CLAUDE.md R8).
 */
@Entity
@Table(name = "recap_link")
public class RecapLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID sessionId;
    private String channel;
    private String valueHash;
    private boolean consent;
    private Instant linkedAt;

    protected RecapLink() {
    }

    public RecapLink(UUID sessionId, String channel, String valueHash, boolean consent, Instant linkedAt) {
        this.sessionId = sessionId;
        this.channel = channel;
        this.valueHash = valueHash;
        this.consent = consent;
        this.linkedAt = linkedAt;
    }
}
