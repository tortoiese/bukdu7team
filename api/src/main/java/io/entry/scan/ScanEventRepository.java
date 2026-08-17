package io.entry.scan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScanEventRepository extends JpaRepository<ScanEvent, UUID> {

    List<ScanEvent> findBySessionIdOrderByScannedAtAsc(UUID sessionId);

    long countBySessionIdAndProductId(UUID sessionId, String productId);
}
