package io.entry.common;

/**
 * 모든 응답에 붙는 메타 정보. docs/API_CONTRACT.md 0장 참고.
 * sessionRotated는 SessionInterceptor가 RequestContext에 남긴 값을 그대로 반영한다.
 */
public record ApiMeta(String requestId, boolean aiUsed, boolean fallback, Boolean sessionRotated) {

    public static ApiMeta basic() {
        return new ApiMeta(RequestContext.requestId(), false, false, rotatedOrNull());
    }

    public static ApiMeta ai(boolean aiUsed, boolean fallback) {
        return new ApiMeta(RequestContext.requestId(), aiUsed, fallback, rotatedOrNull());
    }

    private static Boolean rotatedOrNull() {
        return RequestContext.sessionRotated() ? Boolean.TRUE : null;
    }
}
