package io.entry.session.dto;

import io.entry.common.AppLocale;
import io.entry.common.Market;
import jakarta.validation.constraints.NotNull;

public record UpdateMarketRequest(@NotNull Market market, @NotNull AppLocale locale) {
}
