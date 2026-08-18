package io.entry.transfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.ai.AiCallExecutor;
import io.entry.ai.AiClient;
import io.entry.ai.AiUnavailableException;
import io.entry.ai.PromptLoader;
import io.entry.common.Market;
import io.entry.intent.UnresolvedCode;
import io.entry.transfer.dto.TransferData;
import org.springframework.stereotype.Component;

/**
 * AI-4: 저장 목록 + 시장별 재고 → 발송 시점 근거 문장과 미해결 요인 답변.
 * AI 실패 시 규칙 기반 rationale 문장 + RuleTransferAnswerService로 폴백한다.
 */
@Component
public class AiTransferAnswerService {

    private static final String FALLBACK_RATIONALE = "마지막으로 확인한 시점 이후 체류가 끝났다고 판단해 72시간 뒤로 제안합니다.";

    private final AiClient aiClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final RuleTransferAnswerService ruleAnswerService;

    public AiTransferAnswerService(AiClient aiClient, PromptLoader promptLoader, ObjectMapper objectMapper,
                                    RuleTransferAnswerService ruleAnswerService) {
        this.aiClient = aiClient;
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
        this.ruleAnswerService = ruleAnswerService;
    }

    public record Result(String rationale, TransferData.UnresolvedAnswer answer) {
    }

    /** unresolvedCode가 없으면(모든 요인이 해소됨) AI를 호출하지 않고 규칙 기반 rationale만 반환한다. */
    public Result resolve(String itemListText, Market targetMarket, UnresolvedCode unresolvedCode, String question) {
        Result fallback = new Result(FALLBACK_RATIONALE,
                unresolvedCode == null ? null : ruleAnswerService.answerFor(unresolvedCode).orElse(null));

        if (unresolvedCode == null || itemListText.isBlank()) {
            return fallback;
        }

        return AiCallExecutor.callWithFallback(
                () -> aiClient.complete("transfer-message", buildPrompt(itemListText, targetMarket, unresolvedCode, question)),
                raw -> parse(raw, unresolvedCode, question),
                () -> fallback
        );
    }

    private String buildPrompt(String itemListText, Market targetMarket, UnresolvedCode unresolvedCode, String question) {
        return promptLoader.load("transfer-message")
                .replace("{{itemList}}", itemListText)
                .replace("{{targetMarket}}", targetMarket.name())
                .replace("{{unresolvedCode}}", unresolvedCode.name())
                .replace("{{unresolvedQuestion}}", question == null ? "" : question);
    }

    private Result parse(String raw, UnresolvedCode unresolvedCode, String question) {
        try {
            JsonNode node = objectMapper.readTree(AiCallExecutor.extractJsonObject(raw));
            String rationale = node.get("rationale").asText();
            String answerText = node.get("answer").asText();
            return new Result(rationale, new TransferData.UnresolvedAnswer(unresolvedCode, question, answerText, true));
        } catch (Exception e) {
            throw new AiUnavailableException("transfer-message 응답 파싱 실패", e);
        }
    }
}
