package io.entry.advisor.dto;

import java.time.Instant;

public record ConsentResponse(String grantToken, Instant expiresAt, String qrPayload) {
}
