package io.entry.scan.dto;

import io.entry.catalog.Product;

/** /dev/qr QR 시트처럼 전 제품 목록이 필요한 화면 전용 — 재고 조회 없이 가벼운 요약만 담는다. */
public record ProductSummary(String productId, String displayName, String line) {
    public static ProductSummary of(Product product) {
        return new ProductSummary(product.productId(), product.displayName(), product.line());
    }
}
