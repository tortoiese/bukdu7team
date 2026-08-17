package io.entry.passport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PassportRepository extends JpaRepository<Passport, UUID> {
    Optional<Passport> findBySessionId(UUID sessionId);
}
