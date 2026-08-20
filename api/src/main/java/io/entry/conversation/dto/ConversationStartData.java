package io.entry.conversation.dto;

import io.entry.common.CharacterId;
import java.util.List;
import java.util.UUID;

public record ConversationStartData(UUID conversationId, int turnsRemaining, List<Message> messages) {
    public record Message(String role, CharacterId character, String text) {
    }
}
