package io.entry.intent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.ai.AiCallExecutor;
import io.entry.ai.AiClient;
import io.entry.ai.AiUnavailableException;
import io.entry.ai.PromptLoader;
import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI-1: 스캔 시퀀스 → 의도 신호. AI 실패(호출 실패·파싱 실패)시 RuleIntentAnalyzer로 폴백한다(CLAUDE.md 6장).
 */
@Component
public class AiIntentService {

    private final AiClient aiClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final RuleIntentAnalyzer fallback;
    private final ProductCatalog productCatalog;

    public AiIntentService(AiClient aiClient, PromptLoader promptLoader, ObjectMapper objectMapper,
                            RuleIntentAnalyzer fallback, ProductCatalog productCatalog) {
        this.aiClient = aiClient;
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
        this.fallback = fallback;
        this.productCatalog = productCatalog;
    }

    public IntentSignal analyze(List<String> scannedProductIdsInOrder, String currentProductId) {
        return AiCallExecutor.callWithFallback(
                () -> aiClient.complete("intent-signal", buildPrompt(scannedProductIdsInOrder, currentProductId)),
                this::parse,
                () -> fallback.analyze(scannedProductIdsInOrder, currentProductId)
        );
    }

    private String buildPrompt(List<String> scannedProductIdsInOrder, String currentProductId) {
        StringBuilder sequence = new StringBuilder();
        for (String id : scannedProductIdsInOrder) {
            Product p = productCatalog.get(id);
            sequence.append("- ").append(p.line()).append(" / ").append(p.displayName())
                    .append(" (size ").append(p.sizeOrigin()).append(")\n");
        }
        return promptLoader.load("intent-signal")
                .replace("{{scanSequence}}", sequence.toString())
                .replace("{{currentProductId}}", currentProductId);
    }

    private IntentSignal parse(String raw) {
        try {
            JsonNode node = objectMapper.readTree(AiCallExecutor.extractJsonObject(raw));
            return new IntentSignal(
                    IntentStage.valueOf(node.get("stage").asText()),
                    node.get("comparisonAxis").asText(),
                    UnresolvedCode.valueOf(node.get("unresolved").asText()),
                    node.get("confidence").asDouble(),
                    node.get("rationale").asText(),
                    true
            );
        } catch (Exception e) {
            throw new AiUnavailableException("intent-signal 응답 파싱 실패", e);
        }
    }
}
