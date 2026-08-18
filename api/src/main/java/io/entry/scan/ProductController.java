package io.entry.scan;

import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.common.Market;
import io.entry.inventory.InventoryViewAssembler;
import io.entry.scan.dto.ProductData;
import io.entry.scan.dto.ProductSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

    private final ProductCatalog productCatalog;
    private final InventoryViewAssembler inventoryViewAssembler;

    public ProductController(ProductCatalog productCatalog, InventoryViewAssembler inventoryViewAssembler) {
        this.productCatalog = productCatalog;
        this.inventoryViewAssembler = inventoryViewAssembler;
    }

    // /dev/qr QR 시트 전용 — 재고 조회 없는 가벼운 목록. API_CONTRACT.md 2장에 문서화됨.
    @GetMapping("/api/v1/products")
    public ApiResponse<List<ProductSummary>> list() {
        List<ProductSummary> summaries = productCatalog.all().stream().map(ProductSummary::of).toList();
        return ApiResponse.of(summaries, ApiMeta.basic());
    }

    @GetMapping("/api/v1/products/{productId}")
    public ApiResponse<ProductData> get(@PathVariable String productId, @RequestParam Market market) {
        Product product = productCatalog.get(productId);
        var stock = inventoryViewAssembler.productStock(productId, market);
        return ApiResponse.of(ProductData.of(product, stock), ApiMeta.basic());
    }
}
