package io.entry.archive;

import io.entry.archive.dto.IntentSummary;
import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 저장 목록을 관측 사실 문장으로 요약한다. Phase 5에서 AI(recap-summary)가 붙기 전까지의 폴백이다. */
@Component
public class RuleArchiveSummaryService {

    private final ProductCatalog productCatalog;

    public RuleArchiveSummaryService(ProductCatalog productCatalog) {
        this.productCatalog = productCatalog;
    }

    public IntentSummary summarize(List<SavedItem> items) {
        if (items.isEmpty()) {
            return new IntentSummary("아직 저장한 제품이 없습니다.", false);
        }

        Map<String, Long> lineCounts = new LinkedHashMap<>();
        Map<String, Long> materialCounts = new LinkedHashMap<>();
        for (SavedItem item : items) {
            Product product = productCatalog.get(item.getProductId());
            lineCounts.merge(product.line(), 1L, Long::sum);
            materialCounts.merge(product.material(), 1L, Long::sum);
        }

        String topLine = topKey(lineCounts);
        String topMaterial = topKey(materialCounts);

        String text = String.format("%s 라인을 중심으로 %s 소재 제품을 %d개 저장했습니다.", topLine, topMaterial, items.size());
        return new IntentSummary(text, false);
    }

    private String topKey(Map<String, Long> counts) {
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }
}
