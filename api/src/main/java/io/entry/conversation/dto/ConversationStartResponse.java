package io.entry.conversation.dto;

import java.util.List;

public record ConversationStartResponse(String conversationId, int turnsRemaining, List<MessageData> messages) {
}
