package io.entry.passport;

import jakarta.validation.constraints.NotBlank;

public record PassportRequestDto(@NotBlank String popupId, @NotBlank String issuedAtStore) {
}
