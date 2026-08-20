package io.entry.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<AnonymousSession, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from AnonymousSession session where session.id = :id")
    Optional<AnonymousSession> findLockedById(@Param("id") UUID id);
}
