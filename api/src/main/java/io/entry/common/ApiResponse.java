package io.entry.common;

/**
 * 전 엔드포인트 공통 응답 래퍼. { "data": ..., "meta": ... }
 */
public record ApiResponse<T>(T data, ApiMeta meta) {

    public static <T> ApiResponse<T> of(T data, ApiMeta meta) {
        return new ApiResponse<>(data, meta);
    }
}
