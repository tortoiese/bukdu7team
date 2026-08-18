package io.entry.scan;

import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import io.entry.intent.AiIntentService;
import io.entry.intent.IntentSignal;
import io.entry.scan.dto.ScanRequest;
import io.entry.scan.dto.ScanResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ScanService {

    private final ScanEventRepository scanEventRepository;
    private final ProductCatalog productCatalog;
    private final AiIntentService intentAnalyzer;
    private final AiGreetingService greetingService;

    public ScanService(ScanEventRepository scanEventRepository, ProductCatalog productCatalog,
                        AiIntentService intentAnalyzer, AiGreetingService greetingService) {
        this.scanEventRepository = scanEventRepository;
        this.productCatalog = productCatalog;
        this.intentAnalyzer = intentAnalyzer;
        this.greetingService = greetingService;
    }

    @Transactional
    public ScanResponse recordScan(UUID sessionId, ScanRequest request) {
        Product product = productCatalog.get(request.productId()); // 존재하지 않으면 PRODUCT_NOT_FOUND

        ScanEvent event = new ScanEvent(sessionId, request.productId(), request.storeId(), request.zoneId(), Instant.now());
        scanEventRepository.save(event);

        List<ScanEvent> history = scanEventRepository.findBySessionIdOrderByScannedAtAsc(sessionId);
        List<String> productIdsInOrder = history.stream().map(ScanEvent::getProductId).toList();

        IntentSignal intentSignal = intentAnalyzer.analyze(productIdsInOrder, request.productId());
        long scanCountForProduct = scanEventRepository.countBySessionIdAndProductId(sessionId, request.productId());
        var greeting = greetingService.greet(product, intentSignal, (int) scanCountForProduct);

        return new ScanResponse(event.getId().toString(), scanCountForProduct, history.size(), intentSignal, greeting);
    }
}
