package io.entry.scan.dto;

import io.entry.catalog.CraftNote;
import io.entry.catalog.Product;
import io.entry.inventory.ProductStockView;

import java.util.List;

public record ProductData(
        String productId,
        String line,
        String displayName,
        String material,
        int weightGram,
        SizeLabel sizeLabel,
        List<CraftNote> craftNotes,
        List<String> media,
        ProductStockView stock,
        String priceDisplay
) {
    public record SizeLabel(String local, String origin) {
    }

    public static ProductData of(Product product, ProductStockView stock) {
        return new ProductData(
                product.productId(),
                product.line(),
                product.displayName(),
                product.material(),
                product.weightGram(),
                new SizeLabel(product.sizeLocal(), product.sizeOrigin()),
                product.craftNotes(),
                List.of(),
                stock,
                null
        );
    }
}
