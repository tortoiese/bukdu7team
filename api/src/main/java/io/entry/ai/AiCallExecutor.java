package io.entry.ai;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * AI 호출 → 파싱 절차의 공통 뼈대. 실패(호출 실패든 파싱 실패든) 시 1회 재시도하고,
 * 그래도 실패하면 규칙 기반 폴백으로 넘어간다(CLAUDE.md 6장 "파싱 실패 시 1회 재시도 후 폴백").
 */
public final class AiCallExecutor {

    private static final int MAX_ATTEMPTS = 2;

    private AiCallExecutor() {
    }

    public static <T> T callWithFallback(Supplier<String> call, Function<String, T> parse, Supplier<T> fallback) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                return parse.apply(call.get());
            } catch (Exception ignored) {
                // 다음 시도로 넘어가거나(마지막 시도였다면) 아래에서 폴백으로 전환한다.
            }
        }
        return fallback.get();
    }

    /** 모델 응답 앞뒤에 설명이 섞여도 JSON 객체 부분만 추출한다. */
    public static String extractJsonObject(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end < 0 || end < start) {
            throw new IllegalStateException("응답에서 JSON 객체를 찾을 수 없습니다.");
        }
        return raw.substring(start, end + 1);
    }
}
