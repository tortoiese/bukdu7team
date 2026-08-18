package io.entry.conversation.dto;

import jakarta.validation.constraints.NotBlank;

public record ConversationStartRequest(@NotBlank String scanId) {
}
