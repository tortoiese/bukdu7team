package io.entry.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** docs/API_CONTRACT.md 0장 locale enum: ko | en | zh-Hant | ja */
public enum AppLocale {
    KO("ko"), EN("en"), ZH_HANT("zh-Hant"), JA("ja");

    private final String value;

    AppLocale(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static AppLocale from(String value) {
        for (AppLocale locale : values()) {
            if (locale.value.equalsIgnoreCase(value)) return locale;
        }
        throw new IllegalArgumentException("지원하지 않는 locale입니다: " + value);
    }
}
