package io.entry.passport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PassportStampRepository extends JpaRepository<PassportStamp, UUID> {
    List<PassportStamp> findByPassportIdOrderByStampedAtAsc(UUID passportId);

    Optional<PassportStamp> findByPassportIdAndZoneId(UUID passportId, String zoneId);

    Optional<PassportStamp> findTopByPassportIdOrderByStampedAtDesc(UUID passportId);
}
