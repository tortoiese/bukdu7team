package io.entry.common;

/**
 * 요청별 requestId를 스레드에 보관한다. RequestIdFilter가 요청 시작 시 채우고 끝에 비운다.
 */
public final class RequestContext {

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> SESSION_ID = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SESSION_ROTATED = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }

    public static String requestId() {
        String id = REQUEST_ID.get();
        return id != null ? id : "unknown";
    }

    /** SessionInterceptor가 매 요청마다 채운다. 세션이 필요 없는 엔드포인트에서는 null일 수 있다. */
    public static void setSessionId(String sessionId, boolean rotated) {
        SESSION_ID.set(sessionId);
        SESSION_ROTATED.set(rotated);
    }

    public static String sessionId() {
        return SESSION_ID.get();
    }

    public static boolean sessionRotated() {
        return Boolean.TRUE.equals(SESSION_ROTATED.get());
    }

    public static void clear() {
        REQUEST_ID.remove();
        SESSION_ID.remove();
        SESSION_ROTATED.remove();
    }
}
