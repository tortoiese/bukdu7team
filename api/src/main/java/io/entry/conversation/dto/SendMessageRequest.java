package io.entry.conversation.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(@NotBlank String text) {
}
