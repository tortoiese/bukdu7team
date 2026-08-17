package io.entry.recap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecapLinkRepository extends JpaRepository<RecapLink, UUID> {
}
