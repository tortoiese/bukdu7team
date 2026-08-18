package io.entry.conversation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.ai.AiCallExecutor;
import io.entry.ai.AiClient;
import io.entry.ai.AiUnavailableException;
import io.entry.ai.PromptLoader;
import io.entry.catalog.Product;
import io.entry.common.CharacterId;
import io.entry.intent.UnresolvedCode;
import io.entry.scan.RuleGreetingService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI-2(대화): 최대 3턴 대화의 캐릭터 응답을 생성한다. 캐릭터는 P1 인사와 동일하게 제품으로부터
 * 결정론적으로 고른다(같은 제품이면 대화 내내 같은 캐릭터). AI 실패 시 규칙 기반 폴백으로 전환한다.
 */
@Component
public class AiConversationService {

    public record Result(CharacterId character, String message, List<String> criteria, UnresolvedCode unresolved, boolean aiUsed) {
    }

    private final AiClient aiClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;

    public AiConversationService(AiClient aiClient, PromptLoader promptLoader, ObjectMapper objectMapper) {
        this.aiClient = aiClient;
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
    }

    public Result reply(Product product, List<ConversationMessage> transcript, String userMessage, int turnNumber) {
        CharacterId character = RuleGreetingService.characterFor(product);
        return AiCallExecutor.callWithFallback(
                () -> aiClient.complete("conversation-system", buildPrompt(character, product, transcript, userMessage, turnNumber)),
                raw -> parse(raw, character),
                () -> fallback(character)
        );
    }

    private String buildPrompt(CharacterId character, Product product, List<ConversationMessage> transcript,
                                String userMessage, int turnNumber) {
        StringBuilder transcriptText = new StringBuilder();
        for (ConversationMessage m : transcript) {
            transcriptText.append(m.getRole() == ConversationMessage.Role.CHARACTER ? "캐릭터: " : "고객: ")
                    .append(m.getText()).append("\n");
        }
        return promptLoader.load("conversation-system")
                .replace("{{characterName}}", character.name())
                .replace("{{turnNumber}}", String.valueOf(turnNumber))
                .replace("{{productDisplayName}}", product.displayName())
                .replace("{{productLine}}", product.line())
                .replace("{{productMaterial}}", product.material())
                .replace("{{transcript}}", transcriptText.toString())
                .replace("{{userMessage}}", userMessage);
    }

    private Result parse(String raw, CharacterId character) {
        try {
            JsonNode node = objectMapper.readTree(AiCallExecutor.extractJsonObject(raw));
            List<String> criteria = node.path("criteria").isArray()
                    ? objectMapper.convertValue(node.path("criteria"), objectMapper.getTypeFactory().constructCollectionType(List.class, String.class))
                    : List.of();
            UnresolvedCode unresolved = node.hasNonNull("unresolved")
                    ? UnresolvedCode.valueOf(node.get("unresolved").asText())
                    : UnresolvedCode.UNKNOWN;
            return new Result(character, node.get("message").asText(), criteria, unresolved, true);
        } catch (Exception e) {
            throw new AiUnavailableException("conversation-system 응답 파싱 실패", e);
        }
    }

    private Result fallback(CharacterId character) {
        return new Result(character, "확실하지 않은 부분은 매장 직원에게 확인해보시는 걸 권해드립니다.", List.of(), UnresolvedCode.UNKNOWN, false);
    }
}
