package io.entry.common;

/**
 * 모든 응답에 붙는 메타 정보. docs/API_CONTRACT.md 0장 참고.
 */
public record ApiMeta(String requestId, boolean aiUsed, boolean fallback, Boolean sessionRotated) {

    public static ApiMeta basic() {
        return new ApiMeta(RequestContext.requestId(), false, false, null);
    }

    public static ApiMeta ai(boolean aiUsed, boolean fallback) {
        return new ApiMeta(RequestContext.requestId(), aiUsed, fallback, null);
    }

    public ApiMeta withSessionRotated() {
        return new ApiMeta(requestId, aiUsed, fallback, true);
    }
}
