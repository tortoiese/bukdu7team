package io.entry.scan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.ai.AiCallExecutor;
import io.entry.ai.AiClient;
import io.entry.ai.AiUnavailableException;
import io.entry.ai.PromptLoader;
import io.entry.catalog.Product;
import io.entry.common.CharacterId;
import io.entry.intent.IntentSignal;
import io.entry.scan.dto.GreetingData;
import org.springframework.stereotype.Component;

/**
 * AI-2: 캐릭터 인사. 캐릭터 선택은 RuleGreetingService와 같은 결정론적 규칙을 그대로 쓰고
 * 문장만 AI가 톤에 맞게 생성한다 — 리렌더마다 캐릭터가 바뀌면 안 되기 때문이다.
 * AI 실패 시 RuleGreetingService로 폴백한다.
 */
@Component
public class AiGreetingService {

    private final AiClient aiClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final RuleGreetingService fallback;

    public AiGreetingService(AiClient aiClient, PromptLoader promptLoader, ObjectMapper objectMapper,
                              RuleGreetingService fallback) {
        this.aiClient = aiClient;
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
        this.fallback = fallback;
    }

    public GreetingData greet(Product product, IntentSignal intent, int scanCountForProduct) {
        CharacterId character = RuleGreetingService.characterFor(product);
        return AiCallExecutor.callWithFallback(
                () -> aiClient.complete("greeting", buildPrompt(character, product, intent, scanCountForProduct)),
                raw -> parse(raw, character),
                () -> fallback.greet(product, intent, scanCountForProduct)
        );
    }

    private String buildPrompt(CharacterId character, Product product, IntentSignal intent, int scanCountForProduct) {
        return promptLoader.load("greeting")
                .replace("{{characterName}}", character.name())
                .replace("{{productDisplayName}}", product.displayName())
                .replace("{{scanCount}}", String.valueOf(scanCountForProduct))
                .replace("{{unresolved}}", intent.unresolved().name());
    }

    private GreetingData parse(String raw, CharacterId character) {
        try {
            JsonNode node = objectMapper.readTree(AiCallExecutor.extractJsonObject(raw));
            return new GreetingData(character, node.get("message").asText());
        } catch (Exception e) {
            throw new AiUnavailableException("greeting 응답 파싱 실패", e);
        }
    }
}
