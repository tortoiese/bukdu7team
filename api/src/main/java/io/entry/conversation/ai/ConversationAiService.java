package io.entry.conversation.ai;

import org.springframework.stereotype.Service;

@Service
public class ConversationAiService {

    private final ConversationAiClient client;
    private final RuleConversationFallback fallback;

    public ConversationAiService(ConversationAiClient client, RuleConversationFallback fallback) {
        this.client = client;
        this.fallback = fallback;
    }

    public AiConversationReply reply(AiConversationContext context) {
        try {
            AiConversationReply reply = client.generate(context);
            if (reply == null || reply.message() == null || reply.message().isBlank()
                    || reply.criteria() == null || reply.unresolved() == null) {
                return fallback.generate(context);
            }
            return reply;
        } catch (RuntimeException ex) {
            return fallback.generate(context);
        }
    }
}
