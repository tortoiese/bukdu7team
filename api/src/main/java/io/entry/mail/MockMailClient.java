package io.entry.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 개발/데모용. 실제로 보내지 않고 로그에만 남긴다 — 발표 중 실제 메일함을 못 열어도
 * 서버 로그로 "발송됐다면 이런 내용" 을 그대로 보여줄 수 있다.
 */
public class MockMailClient implements MailClient {

    private static final Logger log = LoggerFactory.getLogger(MockMailClient.class);

    @Override
    public void send(String to, String subject, String bodyText) {
        log.info("[MOCK MAIL] to={} subject={}\n{}", to, subject, bodyText);
    }
}
