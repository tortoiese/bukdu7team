package io.entry.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Chat Completions API 호출. AnthropicClient와 동일한 AiClient 계약을 구현한다.
 * 프론트에는 절대 API 키를 노출하지 않고 이 백엔드 클라이언트를 통해서만 호출한다(CLAUDE.md R6).
 * entry.ai.mock=true(기본값)면 이 클라이언트 대신 MockAiClient가 쓰이고, mock=false에서
 * entry.ai.provider=openai일 때만 이 클라이언트가 선택된다(AiClientConfig 참고).
 */
public class OpenAiClient implements AiClient {

    private final WebClient webClient;
    private final AiProperties properties;
    private final String apiKey;

    public OpenAiClient(WebClient.Builder webClientBuilder, AiProperties properties, String apiKey) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com").build();
        this.properties = properties;
        this.apiKey = apiKey;
    }

    @Override
    public String complete(String promptKey, String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiUnavailableException("OPENAI_API_KEY가 설정되지 않았습니다: " + promptKey);
        }

        Map<String, Object> body = Map.of(
                "model", properties.getOpenaiModel(),
                "max_tokens", 1024,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            JsonNode response = webClient.post()
                    .uri("/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .retryWhen(Retry.max(1))
                    .block();

            if (response == null) {
                throw new AiUnavailableException("응답이 비어 있습니다: " + promptKey);
            }
            JsonNode choices = response.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new AiUnavailableException("응답 형식이 예상과 다릅니다: " + promptKey);
            }
            return choices.get(0).path("message").path("content").asText();
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUnavailableException("AI 호출 실패: " + promptKey, e);
        }
    }
}
