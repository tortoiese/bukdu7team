package io.entry.passport.dto;

import jakarta.validation.constraints.NotBlank;

/** storeId는 선택값 — 옛 QR(매장 정보 없음)과의 호환을 위해 없으면 기본 매장으로 폴백한다. */
public record StampZoneRequest(@NotBlank String zoneId, String storeId) {
}
