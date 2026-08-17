package io.entry.common;

/**
 * 요청별 requestId를 스레드에 보관한다. RequestIdFilter가 요청 시작 시 채우고 끝에 비운다.
 */
public final class RequestContext {

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }

    public static String requestId() {
        String id = REQUEST_ID.get();
        return id != null ? id : "unknown";
    }

    public static void clear() {
        REQUEST_ID.remove();
    }
}
