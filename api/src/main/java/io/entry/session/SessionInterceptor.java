package io.entry.session;

import io.entry.common.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * X-Entry-Session 헤더를 검증한다. 세션이 없거나 무효해도 401을 내지 않고
 * 새 세션을 자동 발급한 뒤 정상 처리한다(CLAUDE.md 5장). 발급 여부는
 * RequestContext에 남겨 ApiMeta.sessionRotated로 응답에 반영된다.
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Entry-Session";

    private final SessionService sessionService;

    public SessionInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader(HEADER);
        AnonymousSession session = sessionService.findValid(header);

        boolean rotated = session == null;
        AnonymousSession resolved = rotated ? sessionService.autoIssue() : session;
        String sessionId = resolved.getId().toString();

        RequestContext.setSessionId(sessionId, rotated);
        response.setHeader(HEADER, sessionId);
        return true;
    }
}
