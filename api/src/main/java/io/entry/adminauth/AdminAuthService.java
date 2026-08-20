package io.entry.adminauth;

import io.entry.adminauth.dto.AdminLoginRequest;
import io.entry.adminauth.dto.AdminLoginResponse;
import io.entry.common.EntryException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AdminAuthService {

    private final AdminSessionRepository repository;
    private final AdminAuthProperties properties;

    public AdminAuthService(AdminSessionRepository repository, AdminAuthProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        if (!properties.getPassword().equals(request.password())) {
            throw EntryException.unauthorized("ADMIN_PASSWORD_INVALID", "비밀번호가 올바르지 않습니다.");
        }
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getTokenTtlHours(), ChronoUnit.HOURS);
        AdminSession session = new AdminSession(UUID.randomUUID(), now, expiresAt);
        repository.save(session);
        return new AdminLoginResponse(session.getId().toString(), expiresAt);
    }

    /** AdminAuthInterceptor 전용. 토큰이 없거나, 형식이 아니거나, 만료됐으면 false. */
    public boolean isValid(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return false;
        UUID id;
        try {
            id = UUID.fromString(rawToken);
        } catch (IllegalArgumentException notAUuid) {
            return false;
        }
        return repository.findById(id)
                .map(session -> !session.isExpired(Instant.now()))
                .orElse(false);
    }
}
