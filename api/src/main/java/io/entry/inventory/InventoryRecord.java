package io.entry.inventory;

import io.entry.common.Market;
import io.entry.common.StockStatus;

public record InventoryRecord(
        String productId,
        String storeId,
        String storeName,
        Market market,
        StockStatus status,
        Integer transferDays,
        String onlineUrl
) {
}
