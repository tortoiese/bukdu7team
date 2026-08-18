package io.entry.advisor.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ConsentRequest(@NotEmpty List<String> scope, @Positive int ttlSeconds) {
}
