package io.entry.common;

import org.springframework.http.HttpStatus;

/**
 * 도메인 예외 공통 타입. code는 API_CONTRACT.md의 에러 코드와 1:1로 맞춘다.
 */
public class EntryException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public EntryException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public static EntryException notFound(String code, String message) {
        return new EntryException(code, message, HttpStatus.NOT_FOUND);
    }

    public static EntryException badRequest(String code, String message) {
        return new EntryException(code, message, HttpStatus.BAD_REQUEST);
    }

    public static EntryException conflict(String code, String message) {
        return new EntryException(code, message, HttpStatus.CONFLICT);
    }

    public static EntryException unauthorized(String code, String message) {
        return new EntryException(code, message, HttpStatus.UNAUTHORIZED);
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}
