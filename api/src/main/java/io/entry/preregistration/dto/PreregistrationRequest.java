package io.entry.preregistration.dto;

import io.entry.common.Market;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PreregistrationRequest(
        @NotBlank String channel,
        @NotBlank String value,
        @NotEmpty List<String> interestedLines,
        @NotNull Market market,
        boolean consent
) {
}
