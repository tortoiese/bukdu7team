package io.entry.mail;

/**
 * 이메일 발송 공통 인터페이스. entry.mail.mock=true(기본값)면 MockMailClient가,
 * false면 ResendMailClient가 빈으로 등록된다(MailClientConfig). io.entry.ai.AiClient와 동일한 패턴.
 * 실패해도 화면이 막히면 안 되므로 호출부(RecapService)는 항상 실패를 흡수하고 성공 여부만 응답에 담는다.
 */
public interface MailClient {
    void send(String to, String subject, String bodyText);
}
