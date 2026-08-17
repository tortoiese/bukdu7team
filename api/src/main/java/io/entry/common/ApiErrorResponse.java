package io.entry.common;

public record ApiErrorResponse(ApiErrorDetail error) {

    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(new ApiErrorDetail(code, message));
    }
}
