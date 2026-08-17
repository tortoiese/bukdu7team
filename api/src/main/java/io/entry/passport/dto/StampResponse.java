package io.entry.passport.dto;

import io.entry.passport.PassportTier;

import java.time.Instant;

public record StampResponse(
        String zoneId,
        Instant stampedAt,
        int rotationSeed,
        int accessTier,
        boolean tierUnlocked,
        PassportTier.NextTier nextTier
) {
}
