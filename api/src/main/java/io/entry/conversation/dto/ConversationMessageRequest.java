package io.entry.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConversationMessageRequest(
        @NotBlank @Size(max = 500) String text
) {
}
