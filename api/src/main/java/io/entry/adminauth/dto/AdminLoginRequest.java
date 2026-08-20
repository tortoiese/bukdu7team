package io.entry.adminauth.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(@NotBlank String password) {
}
