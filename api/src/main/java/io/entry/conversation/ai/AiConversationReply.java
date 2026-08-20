package io.entry.conversation.ai;

import io.entry.intent.UnresolvedCode;
import java.util.List;

public record AiConversationReply(
        String message,
        List<String> criteria,
        UnresolvedCode unresolved,
        boolean aiUsed,
        boolean fallback
) {
}
