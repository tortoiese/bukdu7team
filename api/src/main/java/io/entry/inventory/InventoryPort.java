package io.entry.inventory;

import java.util.List;

/**
 * 재고 조회 경계. 실제 옴니채널 재고 시스템과 연동할 때는 이 인터페이스의 구현만 교체하면 된다.
 * 지금은 DummyInventoryAdapter가 seed/inventory.json을 조회해 응답한다.
 */
public interface InventoryPort {

    /** 특정 제품의 전 매장 재고 항목. 호출부에서 storeId/market으로 걸러 쓴다. */
    List<InventoryRecord> byProduct(String productId);
}
