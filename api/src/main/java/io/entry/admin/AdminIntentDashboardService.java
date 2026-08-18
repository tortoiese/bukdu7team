package io.entry.admin;

import io.entry.admin.dto.IntentDashboardData;
import io.entry.archive.SavedItem;
import io.entry.archive.SavedItemRepository;
import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import io.entry.catalog.Zone;
import io.entry.catalog.ZoneCatalog;
import io.entry.common.Market;
import io.entry.intent.IntentService;
import io.entry.intent.UnresolvedCode;
import io.entry.recap.RecapLinkRepository;
import io.entry.scan.ScanEvent;
import io.entry.scan.ScanEventRepository;
import io.entry.session.AnonymousSession;
import io.entry.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * D1 의도 대시보드. 해커톤 더미 데이터 범위에서 실제로 수집되는 이벤트(스캔·저장·계정연결)만으로
 * 지표를 계산한다 — 수집하지 않는 값(체류시간 등)은 0으로 두고 지어내지 않는다(CLAUDE.md R3).
 */
@Service
public class AdminIntentDashboardService {

    private final ScanEventRepository scanEventRepository;
    private final SavedItemRepository savedItemRepository;
    private final SessionRepository sessionRepository;
    private final RecapLinkRepository recapLinkRepository;
    private final ProductCatalog productCatalog;
    private final ZoneCatalog zoneCatalog;
    private final IntentService intentService;

    public AdminIntentDashboardService(ScanEventRepository scanEventRepository, SavedItemRepository savedItemRepository,
                                        SessionRepository sessionRepository, RecapLinkRepository recapLinkRepository,
                                        ProductCatalog productCatalog, ZoneCatalog zoneCatalog, IntentService intentService) {
        this.scanEventRepository = scanEventRepository;
        this.savedItemRepository = savedItemRepository;
        this.sessionRepository = sessionRepository;
        this.recapLinkRepository = recapLinkRepository;
        this.productCatalog = productCatalog;
        this.zoneCatalog = zoneCatalog;
        this.intentService = intentService;
    }

    public IntentDashboardData get() {
        List<ScanEvent> allScans = scanEventRepository.findAll();
        List<SavedItem> allSaved = savedItemRepository.findAll();
        List<AnonymousSession> allSessions = sessionRepository.findAll();
        long linkedAccounts = recapLinkRepository.count();

        List<IntentDashboardData.ProductStat> products = productCatalog.all().stream()
                .map(product -> productStat(product, allScans, allSaved))
                .filter(stat -> stat.scans() > 0)
                .sorted(Comparator.comparingLong(IntentDashboardData.ProductStat::scans).reversed())
                .toList();

        List<IntentDashboardData.UnresolvedCount> unresolvedDistribution = unresolvedDistribution(allScans);
        List<IntentDashboardData.MarketCount> marketDistribution = marketDistribution(allSessions);

        long sent = allSaved.size();
        double recoveryRate = sent == 0 ? 0 : (double) linkedAccounts / sent;
        IntentDashboardData.TransferRecovery transferRecovery =
                new IntentDashboardData.TransferRecovery(sent, linkedAccounts, round2(recoveryRate));

        List<IntentDashboardData.ZoneStat> zonePerformance = zoneCatalog.all().stream()
                .map(zone -> zoneStat(zone, allSaved))
                .toList();

        List<String> actionHints = actionHints(unresolvedDistribution, products);

        return new IntentDashboardData(products, unresolvedDistribution, marketDistribution, transferRecovery, zonePerformance, actionHints);
    }

    private IntentDashboardData.ProductStat productStat(Product product, List<ScanEvent> allScans, List<SavedItem> allSaved) {
        List<ScanEvent> productScans = allScans.stream().filter(s -> s.getProductId().equals(product.productId())).toList();
        long scans = productScans.size();

        Map<UUID, Long> scansPerSession = productScans.stream()
                .collect(Collectors.groupingBy(ScanEvent::getSessionId, Collectors.counting()));
        long rescannedSessions = scansPerSession.values().stream().filter(c -> c >= 2).count();
        double rescanRate = scansPerSession.isEmpty() ? 0 : (double) rescannedSessions / scansPerSession.size();

        long saved = allSaved.stream().filter(s -> s.getProductId().equals(product.productId())).count();
        double saveRate = scans == 0 ? 0 : (double) saved / scans;

        // "전환"에 해당하는 실제 구매 데이터가 없어(CLAUDE.md R8), 계정 연결(기록을 잃지 않기 위한 연결)을
        // 가장 가까운 관측 가능 신호로 사용한다.
        Set<UUID> savedSessions = allSaved.stream()
                .filter(s -> s.getProductId().equals(product.productId()))
                .map(SavedItem::getSessionId).collect(Collectors.toSet());
        long linkedAmongSaved = savedSessions.stream().filter(recapLinkRepository::existsBySessionId).count();
        double conversionRate = scans == 0 ? 0 : (double) linkedAmongSaved / scans;

        return new IntentDashboardData.ProductStat(product.productId(), scans, round2(rescanRate), round2(saveRate), round2(conversionRate));
    }

    private List<IntentDashboardData.UnresolvedCount> unresolvedDistribution(List<ScanEvent> allScans) {
        Set<UUID> sessionIds = allScans.stream().map(ScanEvent::getSessionId).collect(Collectors.toSet());
        Map<UnresolvedCode, Long> counts = sessionIds.stream()
                .map(intentService::currentSignal)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.groupingBy(signal -> signal.unresolved(), Collectors.counting()));

        return counts.entrySet().stream()
                .map(e -> new IntentDashboardData.UnresolvedCount(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(IntentDashboardData.UnresolvedCount::count).reversed())
                .toList();
    }

    private List<IntentDashboardData.MarketCount> marketDistribution(List<AnonymousSession> allSessions) {
        Map<Market, Long> counts = allSessions.stream()
                .collect(Collectors.groupingBy(AnonymousSession::getMarket, Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> new IntentDashboardData.MarketCount(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(IntentDashboardData.MarketCount::sessions).reversed())
                .toList();
    }

    private IntentDashboardData.ZoneStat zoneStat(Zone zone, List<SavedItem> allSaved) {
        long saves = allSaved.stream().filter(s -> zone.zoneId().equals(s.getZoneId())).count();
        // 체류시간은 능동 스캔만 기록하는 이 서비스 구조상 수집되지 않는다(CLAUDE.md R3) — 0으로 표시한다.
        return new IntentDashboardData.ZoneStat(zone.zoneId(), 0, saves);
    }

    private List<String> actionHints(List<IntentDashboardData.UnresolvedCount> unresolvedDistribution,
                                      List<IntentDashboardData.ProductStat> products) {
        if (unresolvedDistribution.isEmpty()) return List.of();
        var top = unresolvedDistribution.get(0);
        long affectedProducts = products.stream().filter(p -> p.scans() > 0).count();
        return List.of(top.code() + " 미해결이 집중되어 있습니다. 관련 제품 " + affectedProducts + "개의 태그에 보완 정보를 추가하세요.");
    }

    private double round2(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
