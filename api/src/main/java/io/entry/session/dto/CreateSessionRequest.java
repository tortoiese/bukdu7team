package io.entry.session.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSessionRequest(
        String acceptLanguage,
        String timezone,
        @NotBlank String entryPoint
) {
}
