package io.entry.recap.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record LinkAccountRequest(@NotBlank String channel, @NotBlank String value, @AssertTrue boolean consent) {
}
