package io.entry.persona.dto;

import jakarta.validation.constraints.NotBlank;

public record SimulateRequest(
        @NotBlank String hypothesis,
        @NotBlank String variantA,
        @NotBlank String variantB,
        @NotBlank String productId
) {
}
