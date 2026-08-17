package io.entry.recap.dto;

import io.entry.archive.dto.IntentSummary;
import io.entry.intent.UnresolvedCode;

import java.time.LocalDate;
import java.util.List;

public record RecapData(
        LocalDate visitDate,
        String storeName,
        List<ViewedProduct> viewedProducts,
        IntentSummary interestSummary,
        List<UnresolvedFactor> unresolvedFactors,
        AccountLink accountLink
) {
    public record ViewedProduct(String productId, String displayName, long scanCount, int order) {
    }

    public record UnresolvedFactor(UnresolvedCode code, String label, String productId) {
    }

    public record AccountLink(boolean required, String reason) {
    }
}
