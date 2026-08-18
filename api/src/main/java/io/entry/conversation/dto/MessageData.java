package io.entry.conversation.dto;

import io.entry.common.CharacterId;
import io.entry.conversation.ConversationMessage;

public record MessageData(String role, CharacterId character, String text) {
    public static MessageData of(ConversationMessage message) {
        return new MessageData(message.getRole().name(), message.getCharacter(), message.getText());
    }
}
