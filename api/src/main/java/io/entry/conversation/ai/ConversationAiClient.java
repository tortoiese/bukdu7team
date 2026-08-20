package io.entry.conversation.ai;

@FunctionalInterface
public interface ConversationAiClient {
    AiConversationReply generate(AiConversationContext context);
}
