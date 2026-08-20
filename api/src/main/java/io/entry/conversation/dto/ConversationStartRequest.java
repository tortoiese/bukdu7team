package io.entry.conversation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConversationStartRequest(@NotNull UUID scanId) {
}
