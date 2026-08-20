package io.entry.mail;

/**
 * 메일 발송 실패 시 던진다. io.entry.ai.AiUnavailableException과 동일한 패턴 —
 * 호출부(RecapService)가 잡아서 흡수하고 화면은 항상 렌더링된다.
 */
public class MailUnavailableException extends RuntimeException {

    public MailUnavailableException(String message) {
        super(message);
    }

    public MailUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
