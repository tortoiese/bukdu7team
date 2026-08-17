package io.entry.scan.dto;

import jakarta.validation.constraints.NotBlank;

public record ScanRequest(
        @NotBlank String productId,
        @NotBlank String storeId,
        @NotBlank String zoneId,
        String scannedAt
) {
}
