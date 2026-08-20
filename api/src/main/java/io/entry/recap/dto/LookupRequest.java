package io.entry.recap.dto;

import jakarta.validation.constraints.NotBlank;

public record LookupRequest(@NotBlank String email) {
}
