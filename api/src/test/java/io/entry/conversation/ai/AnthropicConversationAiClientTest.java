package io.entry.conversation.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.catalog.Product;
import io.entry.common.CharacterId;
import io.entry.intent.UnresolvedCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AnthropicConversationAiClientTest {

    @Test
    void Anthropic_JSON_응답을_대화_결과로_변환한다() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String structuredText = objectMapper.writeValueAsString(Map.of(
                "message", "스카이 스트림은 가방 무게가 780g입니다.",
                "criteria", List.of("휴대성"),
                "unresolved", "PORTABILITY"));
        String responseBody = objectMapper.writeValueAsString(Map.of(
                "content", List.of(Map.of("type", "text", "text", structuredText))));
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(responseBody)
                        .build()))
                .build();
        AnthropicConversationAiClient client = new AnthropicConversationAiClient(
                webClient, objectMapper, "test-key", "claude-sonnet-5", 8,
                "당신은 럭셔리 팝업스토어의 제품 어드바이저입니다.");

        AiConversationReply reply = client.generate(context());

        assertThat(reply.message()).contains("780g");
        assertThat(reply.criteria()).containsExactly("휴대성");
        assertThat(reply.unresolved()).isEqualTo(UnresolvedCode.PORTABILITY);
        assertThat(reply.aiUsed()).isTrue();
        assertThat(reply.fallback()).isFalse();
    }

    @Test
    void AI_요청에_이전_대화와_현재_질문을_함께_담는다() {
        AnthropicConversationAiClient client = new AnthropicConversationAiClient(
                WebClient.builder().build(), new ObjectMapper(), "test-key", "claude-sonnet-5", 8,
                "당신은 제품 어드바이저입니다.");
        AiConversationContext context = new AiConversationContext(
                product(), CharacterId.KAISER,
                List.of(
                        new AiConversationContext.HistoryMessage("USER", "무게가 궁금해요"),
                        new AiConversationContext.HistoryMessage("CHARACTER", "등록된 무게는 780g입니다")
                ),
                "관리는 어떻게 하나요?");

        String requestText = client.requestBody(context).toString();

        assertThat(requestText).contains("무게가 궁금해요");
        assertThat(requestText).contains("등록된 무게는 780g입니다");
        assertThat(requestText).contains("관리는 어떻게 하나요?");
    }

    private AiConversationContext context() {
        return new AiConversationContext(product(), CharacterId.KAISER, "무겁지 않을까요?");
    }

    private Product product() {
        return new Product(
                "SKY-STREAM-W260", "Sky Stream", "스카이 스트림 백팩",
                "코티드 캔버스", 780, "26cm", "260", List.of());
    }
}
