package io.entry.conversation.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.intent.UnresolvedCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "entry.ai.mock", havingValue = "false")
public class AnthropicConversationAiClient implements ConversationAiClient {

    private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "message", Map.of("type", "string"),
                    "criteria", Map.of("type", "array", "items", Map.of("type", "string")),
                    "unresolved", Map.of("type", "string", "enum", List.of(
                            "SIZE", "COLOR_CARE", "PORTABILITY", "CAPACITY", "GIFT_FIT", "UNKNOWN"))
            ),
            "required", List.of("message", "criteria", "unresolved"),
            "additionalProperties", false
    );

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;
    private final String systemPrompt;

    public AnthropicConversationAiClient(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${entry.ai.api-key:}") String apiKey,
            @Value("${entry.ai.model:claude-sonnet-5}") String model,
            @Value("${entry.ai.timeout-seconds:8}") int timeoutSeconds,
            @Value("classpath:prompts/conversation.md") Resource promptResource
    ) {
        this(webClientBuilder.baseUrl("https://api.anthropic.com").build(), objectMapper,
                apiKey, model, timeoutSeconds, readPrompt(promptResource));
    }

    AnthropicConversationAiClient(WebClient webClient, ObjectMapper objectMapper, String apiKey,
                                  String model, int timeoutSeconds, String systemPrompt) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
        this.systemPrompt = systemPrompt;
    }

    @Override
    public AiConversationReply generate(AiConversationContext context) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY가 설정되지 않았습니다.");
        }

        JsonNode response = webClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .bodyValue(requestBody(context))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(timeoutSeconds));

        return parseResponse(response);
    }

    Map<String, Object> requestBody(AiConversationContext context) {
        String history = context.history().isEmpty()
                ? "(아직 없음)"
                : context.history().stream()
                .map(message -> "%s: %s".formatted(message.role(), message.text()))
                .collect(java.util.stream.Collectors.joining("\n"));
        String craftNotes = context.product().craftNotes().isEmpty()
                ? "(등록된 내용 없음)"
                : context.product().craftNotes().stream()
                .map(note -> "%s: %s".formatted(note.heading(), note.body()))
                .collect(java.util.stream.Collectors.joining("\n"));
        String userContext = """
                캐릭터: %s
                답변 언어: %s
                제품명: %s
                라인: %s
                소재: %s
                무게: %dg
                크기: %s (%s)
                제작 맥락:
                %s
                이전 대화:
                %s
                고객 질문: %s
                """.formatted(
                context.character(), context.locale().value(),
                context.product().displayName(), context.product().line(),
                context.product().material(), context.product().weightGram(),
                context.product().sizeLocal(), context.product().sizeOrigin(),
                craftNotes, history, context.userText());

        return Map.of(
                "model", model,
                "max_tokens", 300,
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userContext)),
                "output_config", Map.of("format", Map.of(
                        "type", "json_schema",
                        "schema", RESPONSE_SCHEMA
                ))
        );
    }

    private AiConversationReply parseResponse(JsonNode response) {
        if (response == null) {
            throw new IllegalStateException("Anthropic 응답이 비어 있습니다.");
        }
        JsonNode content = response.path("content");
        for (JsonNode block : content) {
            if ("text".equals(block.path("type").asText())) {
                try {
                    ConversationPayload payload = objectMapper.readValue(
                            block.path("text").asText(), ConversationPayload.class);
                    return new AiConversationReply(payload.message(), payload.criteria(),
                            payload.unresolved(), true, false);
                } catch (IOException | IllegalArgumentException ex) {
                    throw new IllegalStateException("Anthropic 응답 형식을 해석할 수 없습니다.", ex);
                }
            }
        }
        throw new IllegalStateException("Anthropic 응답에 text 블록이 없습니다.");
    }

    private static String readPrompt(Resource resource) {
        try (var input = resource.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("AI 프롬프트를 읽을 수 없습니다.", ex);
        }
    }

    private record ConversationPayload(
            String message,
            List<String> criteria,
            UnresolvedCode unresolved
    ) {
    }
}
