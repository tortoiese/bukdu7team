package io.entry.transfer;

import io.entry.archive.SavedItem;
import io.entry.archive.SavedItemRepository;
import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import io.entry.common.BrandProperties;
import io.entry.common.CurrencyByMarket;
import io.entry.common.Market;
import io.entry.common.StockStatus;
import io.entry.intent.IntentService;
import io.entry.intent.UnresolvedCode;
import io.entry.inventory.InventoryRecord;
import io.entry.inventory.InventoryViewAssembler;
import io.entry.scan.ScanEvent;
import io.entry.scan.ScanEventRepository;
import io.entry.transfer.dto.TransferData;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransferService {

    private final SavedItemRepository savedItemRepository;
    private final ScanEventRepository scanEventRepository;
    private final ProductCatalog productCatalog;
    private final InventoryViewAssembler inventoryViewAssembler;
    private final IntentService intentService;
    private final RuleTransferAnswerService answerService;
    private final BrandProperties brandProperties;

    public TransferService(SavedItemRepository savedItemRepository, ScanEventRepository scanEventRepository,
                            ProductCatalog productCatalog, InventoryViewAssembler inventoryViewAssembler,
                            IntentService intentService, RuleTransferAnswerService answerService,
                            BrandProperties brandProperties) {
        this.savedItemRepository = savedItemRepository;
        this.scanEventRepository = scanEventRepository;
        this.productCatalog = productCatalog;
        this.inventoryViewAssembler = inventoryViewAssembler;
        this.intentService = intentService;
        this.answerService = answerService;
        this.brandProperties = brandProperties;
    }

    public TransferData get(UUID sessionId, Market targetMarket) {
        List<SavedItem> savedItems = savedItemRepository.findBySessionIdOrderBySavedAtDesc(sessionId);

        List<TransferData.Item> items = savedItems.stream()
                .map(item -> toItem(item, targetMarket))
                .toList();

        List<ScanEvent> history = scanEventRepository.findBySessionIdOrderByScannedAtAsc(sessionId);
        Instant lastScanAt = history.isEmpty() ? Instant.now() : history.get(history.size() - 1).getScannedAt();
        Instant recommendedAt = lastScanAt.plus(Duration.ofHours(72));

        List<TransferData.UnresolvedAnswer> unresolvedAnswers = intentService.currentSignal(sessionId)
                .filter(signal -> signal.unresolved() != UnresolvedCode.UNKNOWN)
                .flatMap(signal -> answerService.answerFor(signal.unresolved()))
                .map(List::of)
                .orElse(List.of());

        return new TransferData(
                brandProperties.getIssuedPlace(),
                targetMarket,
                CurrencyByMarket.of(targetMarket),
                Instant.now(),
                new TransferData.SendTiming(recommendedAt, "마지막으로 확인한 시점 이후 체류가 끝났다고 판단해 72시간 뒤로 제안합니다.", false),
                items,
                unresolvedAnswers,
                new TransferData.MrzTransition("MKT<" + Market.KR, "MKT<" + targetMarket)
        );
    }

    private TransferData.Item toItem(SavedItem savedItem, Market targetMarket) {
        Product product = productCatalog.get(savedItem.getProductId());
        InventoryRecord record = inventoryViewAssembler.marketRecord(savedItem.getProductId(), targetMarket);

        TransferData.Action action = switch (record.status()) {
            case IN_STOCK -> new TransferData.Action("RESERVE", "매장 방문 예약", null);
            case TRANSFERABLE -> new TransferData.Action("REQUEST_TRANSFER", "이동 요청", null);
            case ONLINE_ONLY -> new TransferData.Action("ONLINE", "온라인 스토어에서 주문", record.onlineUrl());
            case OUT_OF_STOCK -> new TransferData.Action("NOTIFY_RESTOCK", "재입고 알림 받기", null);
        };

        return new TransferData.Item(
                savedItem.getProductId(), product.displayName(), record.status(),
                record.status() == StockStatus.IN_STOCK ? record.storeName() : null,
                record.status() == StockStatus.TRANSFERABLE ? record.transferDays() : null,
                record.status() == StockStatus.TRANSFERABLE ? brandProperties.getOriginStoreName() : null,
                action
        );
    }
}
