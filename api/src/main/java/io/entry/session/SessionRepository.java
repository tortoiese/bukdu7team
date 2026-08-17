package io.entry.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SessionRepository extends JpaRepository<AnonymousSession, UUID> {
}
