package io.entry.conversation.dto;

import io.entry.common.CharacterId;
import io.entry.intent.UnresolvedCode;
import java.util.List;

public record ConversationReplyData(
        Reply reply,
        int turnsRemaining,
        Extracted extracted,
        boolean handoffSuggested
) {
    public record Reply(CharacterId character, String message) {
    }

    public record Extracted(List<String> criteria, UnresolvedCode unresolved) {
    }
}
