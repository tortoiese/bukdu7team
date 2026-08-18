package io.entry.archive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.ai.AiCallExecutor;
import io.entry.ai.AiClient;
import io.entry.ai.AiUnavailableException;
import io.entry.ai.PromptLoader;
import io.entry.archive.dto.IntentSummary;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * recap-summary 프롬프트를 공유하는 요약 서비스. P3(리캡)와 P5(아카이브) 양쪽에서
 * "제품 목록 → 관심 경향 1~2문장" 요약이 필요할 때 이 서비스로 AI를 호출하고,
 * 실패 시 호출한 쪽이 넘긴 규칙 기반 폴백으로 넘어간다.
 */
@Component
public class AiRecapSummaryService {

    private final AiClient aiClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;

    public AiRecapSummaryService(AiClient aiClient, PromptLoader promptLoader, ObjectMapper objectMapper) {
        this.aiClient = aiClient;
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
    }

    public IntentSummary summarize(String itemListText, Supplier<IntentSummary> fallback) {
        if (itemListText.isBlank()) {
            return fallback.get();
        }
        return AiCallExecutor.callWithFallback(
                () -> aiClient.complete("recap-summary", promptLoader.load("recap-summary").replace("{{itemList}}", itemListText)),
                this::parse,
                fallback
        );
    }

    private IntentSummary parse(String raw) {
        try {
            JsonNode node = objectMapper.readTree(AiCallExecutor.extractJsonObject(raw));
            return new IntentSummary(node.get("text").asText(), true);
        } catch (Exception e) {
            throw new AiUnavailableException("recap-summary 응답 파싱 실패", e);
        }
    }
}
