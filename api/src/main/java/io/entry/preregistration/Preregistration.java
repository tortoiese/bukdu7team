package io.entry.preregistration;

import io.entry.common.Market;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * P8 사전 등록. 연락 수단은 RecapLink와 같은 방식으로 원문 대신 SHA-256 해시만 저장한다(CLAUDE.md R8).
 * 제공 혜택은 전용 시간대 우선 입장뿐 — 할인·쿠폰 필드를 두지 않는다(CLAUDE.md R2).
 */
@Entity
@Table(name = "preregistration")
public class Preregistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID sessionId;
    private String channel;
    private String valueHash;

    @ElementCollection
    @CollectionTable(name = "preregistration_lines", joinColumns = @JoinColumn(name = "preregistration_id"))
    private List<String> interestedLines;

    private Market market;
    private boolean consent;
    private String code;
    private String timeWindow;
    private Instant createdAt;

    protected Preregistration() {
    }

    public Preregistration(UUID sessionId, String channel, String valueHash, List<String> interestedLines,
                            Market market, boolean consent, String code, String timeWindow, Instant createdAt) {
        this.sessionId = sessionId;
        this.channel = channel;
        this.valueHash = valueHash;
        this.interestedLines = interestedLines;
        this.market = market;
        this.consent = consent;
        this.code = code;
        this.timeWindow = timeWindow;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTimeWindow() {
        return timeWindow;
    }
}
