package io.entry.ai;

/**
 * entry.ai.mock=true(기본값)일 때 실제 Anthropic API를 호출하지 않고 고정 응답을 반환한다.
 * 개발 중 토큰 절약 + 오프라인 개발용. 각 응답은 해당 프롬프트가 요구하는 JSON 스키마를 그대로 흉내낸다.
 */
public class MockAiClient implements AiClient {

    @Override
    public String complete(String promptKey, String prompt) {
        return switch (promptKey) {
            case "intent-signal" -> """
                    {"stage":"SIZE_DECIDED","comparisonAxis":"COLOR","unresolved":"COLOR_CARE","confidence":0.78,
                    "rationale":"같은 라인 안에서 컬러만 바꿔 여러 차례 스캔했습니다."}""";
            case "greeting" -> """
                    {"message":"다시 보시네요. 컬러 때문인가요, 사이즈 때문인가요?"}""";
            case "recap-summary" -> """
                    {"text":"경량 소재와 수납 중심의 제품을 반복해서 살펴보셨습니다."}""";
            case "transfer-message" -> """
                    {"rationale":"체류 마지막 날 이후로 판단해 이 시점으로 이전을 제안합니다.",
                    "answer":"코팅 소재라 물티슈로 닦아 관리할 수 있습니다."}""";
            default -> throw new AiUnavailableException("정의되지 않은 목업 프롬프트: " + promptKey);
        };
    }
}
