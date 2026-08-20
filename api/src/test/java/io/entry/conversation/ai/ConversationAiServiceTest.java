package io.entry.conversation.ai;

import io.entry.catalog.Product;
import io.entry.common.CharacterId;
import io.entry.common.AppLocale;
import io.entry.intent.UnresolvedCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationAiServiceTest {

    private final Product product = new Product(
            "SKY-STREAM-W260", "Sky Stream", "스카이 스트림 백팩",
            "코티드 캔버스", 780, "26cm", "260", List.of());

    @Test
    void AI_클라이언트_응답을_그대로_사용한다() {
        ConversationAiClient client = context -> new AiConversationReply(
                "AI 답변", List.of("관리 용이성"), UnresolvedCode.COLOR_CARE, true, false);
        ConversationAiService service = new ConversationAiService(client, new RuleConversationFallback());

        AiConversationReply reply = service.reply(context("컬러 관리가 고민돼요"));

        assertThat(reply.message()).isEqualTo("AI 답변");
        assertThat(reply.aiUsed()).isTrue();
        assertThat(reply.fallback()).isFalse();
    }

    @Test
    void AI_클라이언트가_실패하면_규칙_응답을_사용한다() {
        ConversationAiClient client = context -> { throw new RuntimeException("timeout"); };
        ConversationAiService service = new ConversationAiService(client, new RuleConversationFallback());

        AiConversationReply reply = service.reply(context("무겁지 않을까요?"));

        assertThat(reply.message()).contains("780g");
        assertThat(reply.unresolved()).isEqualTo(UnresolvedCode.PORTABILITY);
        assertThat(reply.aiUsed()).isFalse();
        assertThat(reply.fallback()).isTrue();
    }

    @Test
    void 영어_세션의_폴백은_영어_질문을_분류하고_영어로_답한다() {
        ConversationAiClient client = context -> { throw new RuntimeException("timeout"); };
        ConversationAiService service = new ConversationAiService(client, new RuleConversationFallback());
        AiConversationContext context = new AiConversationContext(
                product, CharacterId.KAISER, AppLocale.EN, List.of(), "Is it too heavy to carry?");

        AiConversationReply reply = service.reply(context);

        assertThat(reply.message()).contains("780g");
        assertThat(reply.message()).contains("registered weight");
        assertThat(reply.unresolved()).isEqualTo(UnresolvedCode.PORTABILITY);
    }

    private AiConversationContext context(String text) {
        return new AiConversationContext(product, CharacterId.KAISER, text);
    }
}
