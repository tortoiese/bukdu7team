package io.entry.session;

import io.entry.common.AppLocale;
import io.entry.common.EntryException;
import io.entry.common.Market;
import io.entry.session.dto.CreateSessionRequest;
import io.entry.session.dto.SessionData;
import io.entry.session.dto.UpdateMarketRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final MarketLocaleResolver marketLocaleResolver;

    public SessionService(SessionRepository sessionRepository, MarketLocaleResolver marketLocaleResolver) {
        this.sessionRepository = sessionRepository;
        this.marketLocaleResolver = marketLocaleResolver;
    }

    public SessionData create(CreateSessionRequest request) {
        var inferred = marketLocaleResolver.infer(request.acceptLanguage(), request.timezone());
        AnonymousSession session = new AnonymousSession(
                UUID.randomUUID(), inferred.market(), inferred.locale(), request.entryPoint(), Instant.now());
        sessionRepository.save(session);
        return SessionData.of(session, true);
    }

    /** SessionInterceptor 전용. 세션이 없거나 무효할 때 자동 재발급한다. */
    public AnonymousSession autoIssue() {
        AnonymousSession session = new AnonymousSession(
                UUID.randomUUID(), Market.KR, AppLocale.KO, "AUTO_REISSUE", Instant.now());
        return sessionRepository.save(session);
    }

    public AnonymousSession findValid(String rawSessionId) {
        if (rawSessionId == null || rawSessionId.isBlank()) return null;
        try {
            return sessionRepository.findById(UUID.fromString(rawSessionId)).orElse(null);
        } catch (IllegalArgumentException notAUuid) {
            return null;
        }
    }

    public SessionData updateMarket(String sessionId, UpdateMarketRequest request) {
        AnonymousSession session = sessionRepository.findById(UUID.fromString(sessionId))
                .orElseThrow(() -> EntryException.notFound("SESSION_NOT_FOUND", "세션을 찾을 수 없습니다."));
        session.setMarket(request.market());
        session.setLocale(request.locale());
        sessionRepository.save(session);
        return SessionData.of(session, false);
    }
}
