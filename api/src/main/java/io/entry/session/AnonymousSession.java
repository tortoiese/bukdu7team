package io.entry.session;

import io.entry.common.AppLocale;
import io.entry.common.Market;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * 익명 세션. PII 컬럼을 절대 추가하지 않는다(CLAUDE.md R1, 5장).
 */
@Entity
@Table(name = "anonymous_session")
public class AnonymousSession {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Market market;

    @Enumerated(EnumType.STRING)
    private AppLocale locale;

    private String entryPoint;

    private Instant createdAt;

    protected AnonymousSession() {
        // JPA
    }

    public AnonymousSession(UUID id, Market market, AppLocale locale, String entryPoint, Instant createdAt) {
        this.id = id;
        this.market = market;
        this.locale = locale;
        this.entryPoint = entryPoint;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public AppLocale getLocale() {
        return locale;
    }

    public void setLocale(AppLocale locale) {
        this.locale = locale;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
