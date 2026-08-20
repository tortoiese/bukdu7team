package io.entry.adminauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * D1/D2 운영자 화면 접근용 공용 비밀번호 설정. 계정 시스템이 아니라 게스트 메뉴와
 * 완전히 분리된 별도 진입점(/entryadmin)을 지키는 최소한의 장벽이다(CLAUDE.md와 무관한
 * 운영 전용 영역이라 R1의 "로그인 강제 금지"와는 다른 층위).
 */
@Component
@ConfigurationProperties(prefix = "entry.admin")
public class AdminAuthProperties {

    private String password = "entryadmin";
    private int tokenTtlHours = 12;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTokenTtlHours() {
        return tokenTtlHours;
    }

    public void setTokenTtlHours(int tokenTtlHours) {
        this.tokenTtlHours = tokenTtlHours;
    }
}
