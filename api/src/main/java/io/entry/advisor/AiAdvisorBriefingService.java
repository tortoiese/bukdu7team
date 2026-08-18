package io.entry.advisor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.ai.AiCallExecutor;
import io.entry.ai.AiClient;
import io.entry.ai.AiUnavailableException;
import io.entry.ai.PromptLoader;
import io.entry.advisor.dto.AdvisorBriefingData;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * AI-5: 저장 목록 + 미해결 요인 → 응대 직전 브리핑 1~2문장. AI 실패 시 규칙 기반 폴백으로 전환한다.
 */
@Component
public class AiAdvisorBriefingService {

    private final AiClient aiClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;

    public AiAdvisorBriefingService(AiClient aiClient, PromptLoader promptLoader, ObjectMapper objectMapper) {
        this.aiClient = aiClient;
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
    }

    public AdvisorBriefingData.Briefing summarize(String itemListText, String unresolvedListText, String market, String locale,
                                                    Supplier<AdvisorBriefingData.Briefing> fallback) {
        if (itemListText.isBlank()) {
            return fallback.get();
        }
        return AiCallExecutor.callWithFallback(
                () -> aiClient.complete("advisor-briefing", buildPrompt(itemListText, unresolvedListText, market, locale)),
                this::parse,
                fallback
        );
    }

    private String buildPrompt(String itemListText, String unresolvedListText, String market, String locale) {
        return promptLoader.load("advisor-briefing")
                .replace("{{itemList}}", itemListText)
                .replace("{{unresolvedList}}", unresolvedListText)
                .replace("{{market}}", market)
                .replace("{{locale}}", locale);
    }

    private AdvisorBriefingData.Briefing parse(String raw) {
        try {
            JsonNode node = objectMapper.readTree(AiCallExecutor.extractJsonObject(raw));
            return new AdvisorBriefingData.Briefing(node.get("text").asText(), true);
        } catch (Exception e) {
            throw new AiUnavailableException("advisor-briefing 응답 파싱 실패", e);
        }
    }
}
