package io.entry.conversation.ai;

import io.entry.catalog.Product;
import io.entry.common.AppLocale;
import io.entry.common.CharacterId;
import java.util.List;

public record AiConversationContext(
        Product product,
        CharacterId character,
        AppLocale locale,
        List<HistoryMessage> history,
        String userText
) {
    public AiConversationContext(Product product, CharacterId character, String userText) {
        this(product, character, AppLocale.KO, List.of(), userText);
    }

    public AiConversationContext(Product product, CharacterId character,
                                 List<HistoryMessage> history, String userText) {
        this(product, character, AppLocale.KO, history, userText);
    }

    public record HistoryMessage(String role, String text) {
    }
}
