package io.entry.session.dto;

import io.entry.common.AppLocale;
import io.entry.common.Market;
import io.entry.session.AnonymousSession;

import java.time.Instant;

public record SessionData(
        String sessionId,
        Market market,
        AppLocale locale,
        boolean marketInferred,
        Instant createdAt
) {
    public static SessionData of(AnonymousSession session, boolean marketInferred) {
        return new SessionData(
                session.getId().toString(),
                session.getMarket(),
                session.getLocale(),
                marketInferred,
                session.getCreatedAt()
        );
    }
}
