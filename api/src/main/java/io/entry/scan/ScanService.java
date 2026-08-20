package io.entry.scan;

import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import io.entry.intent.AiIntentService;
import io.entry.intent.IntentSignal;
import io.entry.passport.PassportService;
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
    private final PassportService passportService;

    public ScanService(ScanEventRepository scanEventRepository, ProductCatalog productCatalog,
                        AiIntentService intentAnalyzer, AiGreetingService greetingService,
                        PassportService passportService) {
        this.scanEventRepository = scanEventRepository;
        this.productCatalog = productCatalog;
        this.intentAnalyzer = intentAnalyzer;
        this.greetingService = greetingService;
        this.passportService = passportService;
    }

    @Transactional
    public ScanResponse recordScan(UUID sessionId, ScanRequest request) {
        Product product = productCatalog.get(request.productId()); // 존재하지 않으면 PRODUCT_NOT_FOUND

        // 매대 태그 스캔이 곧 입구 태그를 댄 것과 같다 — 별도 "발급" 버튼 없이 첫 스캔에서 조용히 발급한다(멱등).
        // 어느 매장(성수/더현대/합정/문래) QR로 스캔했는지에 따라 발급 정보가 갈린다.
        passportService.issueOrGet(sessionId, request.storeId());

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
