package io.entry.archive.dto;

import jakarta.validation.constraints.NotBlank;

public record ArchiveSaveRequest(@NotBlank String productId, String scanId) {
}
