package io.entry.inventory;

import io.entry.common.Market;
import io.entry.common.StockStatus;

import java.util.List;

public record ProductStockView(
        StockStatus thisStore,
        List<StoreStock> domesticOther,
        HomeMarketStock homeMarket
) {
    public record StoreStock(String storeId, String storeName, StockStatus status) {
    }

    public record HomeMarketStock(Market market, StockStatus status, String storeName) {
    }
}
