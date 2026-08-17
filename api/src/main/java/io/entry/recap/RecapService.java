package io.entry.recap;

import io.entry.archive.dto.IntentSummary;
import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import io.entry.common.BrandProperties;
import io.entry.intent.IntentService;
import io.entry.intent.UnresolvedCode;
import io.entry.recap.dto.LinkAccountRequest;
import io.entry.recap.dto.RecapData;
import io.entry.scan.ScanEvent;
import io.entry.scan.ScanEventRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RecapService {

    private static final Map<UnresolvedCode, String> UNRESOLVED_LABELS = Map.of(
            UnresolvedCode.SIZE, "사이즈 미확정",
            UnresolvedCode.COLOR_CARE, "컬러 관리",
            UnresolvedCode.PORTABILITY, "휴대성",
            UnresolvedCode.CAPACITY, "수납량",
            UnresolvedCode.GIFT_FIT, "선물 적합성",
            UnresolvedCode.UNKNOWN, "미해결 요인"
    );

    private final ScanEventRepository scanEventRepository;
    private final ProductCatalog productCatalog;
    private final IntentService intentService;
    private final RecapLinkRepository recapLinkRepository;
    private final BrandProperties brandProperties;

    public RecapService(ScanEventRepository scanEventRepository, ProductCatalog productCatalog,
                         IntentService intentService, RecapLinkRepository recapLinkRepository,
                         BrandProperties brandProperties) {
        this.scanEventRepository = scanEventRepository;
        this.productCatalog = productCatalog;
        this.intentService = intentService;
        this.recapLinkRepository = recapLinkRepository;
        this.brandProperties = brandProperties;
    }

    public RecapData get(UUID sessionId) {
        List<ScanEvent> history = scanEventRepository.findBySessionIdOrderByScannedAtAsc(sessionId);

        Map<String, Long> countsInOrder = new LinkedHashMap<>();
        for (ScanEvent event : history) {
            countsInOrder.merge(event.getProductId(), 1L, Long::sum);
        }

        List<RecapData.ViewedProduct> viewed = new java.util.ArrayList<>();
        int order = 1;
        for (Map.Entry<String, Long> entry : countsInOrder.entrySet()) {
            Product product = productCatalog.get(entry.getKey());
            viewed.add(new RecapData.ViewedProduct(entry.getKey(), product.displayName(), entry.getValue(), order++));
        }

        IntentSummary summary = summarize(viewed);

        List<RecapData.UnresolvedFactor> unresolvedFactors = intentService.currentSignal(sessionId)
                .filter(signal -> signal.unresolved() != UnresolvedCode.UNKNOWN)
                .map(signal -> {
                    String lastProductId = history.isEmpty() ? null : history.get(history.size() - 1).getProductId();
                    return List.of(new RecapData.UnresolvedFactor(
                            signal.unresolved(), UNRESOLVED_LABELS.get(signal.unresolved()), lastProductId));
                })
                .orElse(List.of());

        Instant visitInstant = history.isEmpty() ? Instant.now() : history.get(0).getScannedAt();

        return new RecapData(
                visitInstant.atZone(ZoneOffset.UTC).toLocalDate(),
                brandProperties.getOriginStoreName(),
                viewed,
                summary,
                unresolvedFactors,
                new RecapData.AccountLink(false, "기록을 잃지 않기 위해 연락 수단을 연결할 수 있습니다. 지금까지 개인정보를 요구하지 않았습니다.")
        );
    }

    public boolean link(UUID sessionId, LinkAccountRequest request) {
        String hash = sha256(request.value());
        recapLinkRepository.save(new RecapLink(sessionId, request.channel(), hash, request.consent(), Instant.now()));
        return true;
    }

    private IntentSummary summarize(List<RecapData.ViewedProduct> viewed) {
        if (viewed.isEmpty()) {
            return new IntentSummary("아직 살펴본 제품이 없습니다.", false);
        }
        Map<String, Long> lineCounts = new LinkedHashMap<>();
        for (RecapData.ViewedProduct v : viewed) {
            String line = productCatalog.get(v.productId()).line();
            lineCounts.merge(line, 1L, Long::sum);
        }
        String topLine = lineCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("");
        return new IntentSummary(topLine + " 라인을 중심으로 " + viewed.size() + "개 제품을 살펴보셨습니다.", false);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
