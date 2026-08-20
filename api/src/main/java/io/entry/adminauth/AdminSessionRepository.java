package io.entry.adminauth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminSessionRepository extends JpaRepository<AdminSession, UUID> {
}
