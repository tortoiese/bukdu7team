package io.entry.ai;

/**
 * AI 호출 실패(타임아웃, 오류 응답, 파싱 실패) 시 던진다.
 * 컨트롤러까지 전파하지 않고 각 도메인 서비스가 잡아 규칙 기반 폴백으로 전환한다 —
 * AI가 실패해도 화면은 반드시 렌더링되어야 한다(CLAUDE.md 6장).
 */
public class AiUnavailableException extends RuntimeException {

    public AiUnavailableException(String message) {
        super(message);
    }

    public AiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
