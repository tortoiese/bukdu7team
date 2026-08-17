package io.entry.archive.dto;

import io.entry.common.StockStatus;

import java.time.Instant;

public record ArchiveItemData(
        String productId,
        String displayName,
        Instant savedAt,
        String savedAtStoreId,
        String zoneId,
        String thumbnail,
        StockStatus homeMarketStatus
) {
}
