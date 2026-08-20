package io.entry.adminauth.dto;

import java.time.Instant;

public record AdminLoginResponse(String adminToken, Instant expiresAt) {
}
