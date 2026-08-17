package io.entry.archive;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedItemRepository extends JpaRepository<SavedItem, UUID> {

    List<SavedItem> findBySessionIdOrderBySavedAtDesc(UUID sessionId);

    Optional<SavedItem> findBySessionIdAndProductId(UUID sessionId, String productId);

    long countBySessionId(UUID sessionId);
}
