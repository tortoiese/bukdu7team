package io.entry.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * entry.ai.*와 같은 mock/실전 스위치 패턴. 기본값은 mock=true — 자격증명 없이도
 * 전 화면이 정상 동작해야 한다. Resend(https://resend.com) REST API를 WebClient로 호출한다 —
 * 새 라이브러리(spring-boot-starter-mail 등) 추가 없이 기존 WebClient 의존성만으로 가능하다.
 */
@Component
@ConfigurationProperties(prefix = "entry.mail")
public class MailProperties {

    private boolean mock = true;
    private String fromAddress = "ENTRY <onboarding@resend.dev>";

    public boolean isMock() {
        return mock;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
}
