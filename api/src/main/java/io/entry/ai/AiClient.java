package io.entry.ai;

/**
 * AI 호출 추상화. entry.ai.mock 값에 따라 AiClientConfig가 AnthropicClient 또는
 * MockAiClient 중 하나를 이 인터페이스의 빈으로 등록한다.
 */
public interface AiClient {

    /**
     * @param promptKey 호출 지점 식별자(로그, MockAiClient 응답 분기용). 예: "intent-signal", "greeting"
     * @param prompt    완성된 프롬프트 전체 텍스트. 응답은 프롬프트가 지시한 형식(대개 JSON) 그대로의 문자열로 온다.
     */
    String complete(String promptKey, String prompt);
}
