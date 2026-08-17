package io.entry.passport.dto;

import jakarta.validation.constraints.NotBlank;

public record StampZoneRequest(@NotBlank String zoneId) {
}
