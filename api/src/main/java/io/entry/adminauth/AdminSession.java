package io.entry.adminauth;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * 운영자 로그인 토큰. 사람 계정이 아니라 /entryadmin 비밀번호 검증 통과 시 발급되는
 * 만료형 토큰이다. PII를 담지 않는다.
 */
@Entity
@Table(name = "admin_session")
public class AdminSession {

    @Id
    private UUID id;

    private Instant issuedAt;

    private Instant expiresAt;

    protected AdminSession() {
        // JPA
    }

    public AdminSession(UUID id, Instant issuedAt, Instant expiresAt) {
        this.id = id;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
