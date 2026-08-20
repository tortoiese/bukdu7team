package io.entry.scan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScanEventRepository extends JpaRepository<ScanEvent, UUID> {

    List<ScanEvent> findBySessionIdOrderByScannedAtAsc(UUID sessionId);

    long countBySessionIdAndProductId(UUID sessionId, String productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select scan from ScanEvent scan where scan.id = :id")
    Optional<ScanEvent> findLockedById(@Param("id") UUID id);
}
