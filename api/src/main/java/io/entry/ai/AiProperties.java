package io.entry.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 호출 설정. entry.ai.mock=true(기본값)면 실제 Anthropic API를 호출하지 않고
 * MockAiClient가 고정 응답을 반환한다(개발 중 토큰 절약, CLAUDE.md 6장).
 */
@Component
@ConfigurationProperties(prefix = "entry.ai")
public class AiProperties {

    private boolean mock = true;
    private String model = "claude-sonnet-5";
    private int timeoutSeconds = 8;

    public boolean isMock() {
        return mock;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
