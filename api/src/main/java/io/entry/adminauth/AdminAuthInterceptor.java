package io.entry.adminauth;

import io.entry.common.EntryException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * D1/D2(대시보드·페르소나봇 콘솔) 전용 API를 X-Entry-Admin-Token 헤더로 지킨다.
 * 게스트 익명 세션(SessionInterceptor)과는 별도 층위 — 여기는 실패 시 자동 재발급하지 않고
 * 그대로 401을 반환한다(관리자 화면은 애초에 /entryadmin으로 격리돼 있어 자동 우회를 허용하지 않는다).
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Entry-Admin-Token";

    private final AdminAuthService adminAuthService;

    public AdminAuthInterceptor(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 프리플라이트(OPTIONS)는 브라우저가 실제 헤더 값을 싣지 않고 보낸다.
        // 여기서 막으면 프리플라이트 자체가 깨져 실제 요청이 아예 나가지 못한다.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader(HEADER);
        if (!adminAuthService.isValid(token)) {
            throw EntryException.unauthorized("ADMIN_AUTH_REQUIRED", "관리자 인증이 필요합니다.");
        }
        return true;
    }
}
