package io.entry.advisor;

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
 * P7 어드바이저 열람 권한. 고객이 공유 범위를 선택해 발급하는 일회성 토큰이다.
 * 세션 종료가 아니라 expiresAt로만 만료를 판단한다(해커톤 범위 단순화).
 */
@Entity
@Table(name = "advisor_grant")
public class AdvisorGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID sessionId;
    private String grantToken;

    @ElementCollection
    @CollectionTable(name = "advisor_grant_scope", joinColumns = @JoinColumn(name = "grant_id"))
    private List<String> scope;

    private Instant expiresAt;
    private Instant createdAt;

    protected AdvisorGrant() {
    }

    public AdvisorGrant(UUID sessionId, String grantToken, List<String> scope, Instant expiresAt, Instant createdAt) {
        this.sessionId = sessionId;
        this.grantToken = grantToken;
        this.scope = scope;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getGrantToken() {
        return grantToken;
    }

    public List<String> getScope() {
        return scope;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
