package io.entry.passport.dto;

import io.entry.passport.PassportTier;

import java.time.Instant;
import java.util.List;

public record PassportData(
        String passportNo,
        Instant issuedAt,
        String issuedPlace,
        String popupId,
        List<ZoneEntry> zones,
        long savedCount,
        int accessTier,
        List<PassportTier.Grant> grants,
        String[] mrz
) {
    public record ZoneEntry(String zoneId, String name, boolean visited, Instant stampedAt, Integer rotationSeed) {
    }
}
