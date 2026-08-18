package io.entry.archive;

import io.entry.archive.dto.ArchiveItemData;
import io.entry.archive.dto.ArchiveListData;
import io.entry.archive.dto.ArchiveSaveRequest;
import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import io.entry.common.Market;
import io.entry.inventory.InventoryViewAssembler;
import io.entry.scan.ScanEvent;
import io.entry.scan.ScanEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ArchiveService {

    private final SavedItemRepository savedItemRepository;
    private final ScanEventRepository scanEventRepository;
    private final ProductCatalog productCatalog;
    private final InventoryViewAssembler inventoryViewAssembler;
    private final RuleArchiveSummaryService ruleSummaryService;
    private final AiRecapSummaryService aiSummaryService;

    public ArchiveService(SavedItemRepository savedItemRepository, ScanEventRepository scanEventRepository,
                           ProductCatalog productCatalog, InventoryViewAssembler inventoryViewAssembler,
                           RuleArchiveSummaryService ruleSummaryService, AiRecapSummaryService aiSummaryService) {
        this.savedItemRepository = savedItemRepository;
        this.scanEventRepository = scanEventRepository;
        this.productCatalog = productCatalog;
        this.inventoryViewAssembler = inventoryViewAssembler;
        this.ruleSummaryService = ruleSummaryService;
        this.aiSummaryService = aiSummaryService;
    }

    @Transactional
    public long save(UUID sessionId, ArchiveSaveRequest request) {
        productCatalog.get(request.productId()); // 존재 검증

        if (savedItemRepository.findBySessionIdAndProductId(sessionId, request.productId()).isPresent()) {
            return savedItemRepository.countBySessionId(sessionId);
        }

        String storeId = "KR-SEONGSU";
        String zoneId = "UNKNOWN";
        if (request.scanId() != null) {
            ScanEvent scan = scanEventRepository.findById(UUID.fromString(request.scanId())).orElse(null);
            if (scan != null && scan.getSessionId().equals(sessionId)) {
                storeId = scan.getStoreId();
                zoneId = scan.getZoneId();
            }
        }

        savedItemRepository.save(new SavedItem(sessionId, request.productId(), storeId, zoneId, Instant.now()));
        return savedItemRepository.countBySessionId(sessionId);
    }

    @Transactional
    public long delete(UUID sessionId, String productId) {
        savedItemRepository.findBySessionIdAndProductId(sessionId, productId).ifPresent(savedItemRepository::delete);
        return savedItemRepository.countBySessionId(sessionId);
    }

    public ArchiveListData list(UUID sessionId, Market market) {
        List<SavedItem> items = savedItemRepository.findBySessionIdOrderBySavedAtDesc(sessionId);

        List<ArchiveItemData> data = items.stream().map(item -> {
            Product product = productCatalog.get(item.getProductId());
            var stock = inventoryViewAssembler.productStock(item.getProductId(), market);
            return new ArchiveItemData(
                    item.getProductId(), product.displayName(), item.getSavedAt(),
                    item.getSavedAtStoreId(), item.getZoneId(), "", stock.homeMarket().status());
        }).toList();

        String itemListText = items.stream()
                .map(item -> productCatalog.get(item.getProductId()))
                .map(p -> "- " + p.line() + " / " + p.material())
                .reduce("", (a, b) -> a + b + "\n");

        return new ArchiveListData(data, aiSummaryService.summarize(itemListText, () -> ruleSummaryService.summarize(items)));
    }
}
