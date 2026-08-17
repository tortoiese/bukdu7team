package io.entry.inventory;

import io.entry.common.Market;
import io.entry.common.StockStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/** InventoryPort의 원시 재고 항목을 화면별 응답 형태로 조립한다. */
@Component
public class InventoryViewAssembler {

    private static final String ORIGIN_STORE_ID = "KR-SEONGSU";
    private static final String ORIGIN_STORE_NAME = "성수 팝업";

    private final InventoryPort inventoryPort;

    public InventoryViewAssembler(InventoryPort inventoryPort) {
        this.inventoryPort = inventoryPort;
    }

    public ProductStockView productStock(String productId, Market sessionMarket) {
        List<InventoryRecord> records = inventoryPort.byProduct(productId);

        StockStatus thisStore = records.stream()
                .filter(r -> r.storeId().equals(ORIGIN_STORE_ID))
                .findFirst()
                .map(InventoryRecord::status)
                .orElse(StockStatus.OUT_OF_STOCK);

        List<ProductStockView.StoreStock> domesticOther = records.stream()
                .filter(r -> r.market() == Market.KR && !r.storeId().equals(ORIGIN_STORE_ID))
                .map(r -> new ProductStockView.StoreStock(r.storeId(), r.storeName(), r.status()))
                .toList();

        ProductStockView.HomeMarketStock homeMarket;
        if (sessionMarket == Market.KR) {
            homeMarket = new ProductStockView.HomeMarketStock(Market.KR, thisStore, ORIGIN_STORE_NAME);
        } else {
            homeMarket = records.stream()
                    .filter(r -> r.market() == sessionMarket)
                    .findFirst()
                    .map(r -> new ProductStockView.HomeMarketStock(sessionMarket, r.status(), r.storeName()))
                    .orElse(new ProductStockView.HomeMarketStock(sessionMarket, StockStatus.OUT_OF_STOCK, "-"));
        }

        return new ProductStockView(thisStore, domesticOther, homeMarket);
    }

    public InventoryRecord marketRecord(String productId, Market market) {
        return inventoryPort.byProduct(productId).stream()
                .filter(r -> r.market() == market)
                .findFirst()
                .orElse(new InventoryRecord(productId, "-", "-", market, StockStatus.OUT_OF_STOCK, null, null));
    }
}
