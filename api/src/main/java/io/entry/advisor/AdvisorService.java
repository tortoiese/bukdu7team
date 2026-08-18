package io.entry.advisor;

import io.entry.advisor.dto.AdvisorBriefingData;
import io.entry.advisor.dto.ConsentRequest;
import io.entry.advisor.dto.ConsentResponse;
import io.entry.archive.SavedItem;
import io.entry.archive.SavedItemRepository;
import io.entry.archive.dto.ArchiveItemData;
import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import io.entry.common.EntryException;
import io.entry.intent.IntentService;
import io.entry.intent.UnresolvedCode;
import io.entry.inventory.InventoryViewAssembler;
import io.entry.session.AnonymousSession;
import io.entry.session.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * P7 어드바이저 뷰. 고객이 동의한 범위만 담아 일회용 토큰으로 전달한다.
 */
@Service
public class AdvisorService {

    private static final Map<UnresolvedCode, String> UNRESOLVED_LABELS = Map.of(
            UnresolvedCode.SIZE, "사이즈 미확정",
            UnresolvedCode.COLOR_CARE, "컬러 관리",
            UnresolvedCode.PORTABILITY, "휴대성",
            UnresolvedCode.CAPACITY, "수납량",
            UnresolvedCode.GIFT_FIT, "선물 적합성",
            UnresolvedCode.UNKNOWN, "미해결 요인"
    );

    private final AdvisorGrantRepository grantRepository;
    private final AdvisorNoteRepository noteRepository;
    private final SavedItemRepository savedItemRepository;
    private final SessionRepository sessionRepository;
    private final ProductCatalog productCatalog;
    private final InventoryViewAssembler inventoryViewAssembler;
    private final IntentService intentService;
    private final AiAdvisorBriefingService aiBriefingService;
    private final String webOrigin;

    public AdvisorService(AdvisorGrantRepository grantRepository, AdvisorNoteRepository noteRepository,
                           SavedItemRepository savedItemRepository, SessionRepository sessionRepository,
                           ProductCatalog productCatalog, InventoryViewAssembler inventoryViewAssembler,
                           IntentService intentService, AiAdvisorBriefingService aiBriefingService,
                           @Value("${entry.web.origin}") String webOrigin) {
        this.grantRepository = grantRepository;
        this.noteRepository = noteRepository;
        this.savedItemRepository = savedItemRepository;
        this.sessionRepository = sessionRepository;
        this.productCatalog = productCatalog;
        this.inventoryViewAssembler = inventoryViewAssembler;
        this.intentService = intentService;
        this.aiBriefingService = aiBriefingService;
        this.webOrigin = webOrigin;
    }

    @Transactional
    public ConsentResponse issueConsent(UUID sessionId, ConsentRequest request) {
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plusSeconds(request.ttlSeconds());
        grantRepository.save(new AdvisorGrant(sessionId, token, request.scope(), expiresAt, Instant.now()));
        return new ConsentResponse(token, expiresAt, webOrigin + "/advisor/" + token);
    }

    public AdvisorBriefingData getBriefing(String grantToken) {
        AdvisorGrant grant = requireValidGrant(grantToken);
        UUID sessionId = grant.getSessionId();

        AnonymousSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> EntryException.notFound("SESSION_NOT_FOUND", "세션을 찾을 수 없습니다."));

        List<SavedItem> savedItems = savedItemRepository.findBySessionIdOrderBySavedAtDesc(sessionId);
        List<ArchiveItemData> savedItemData = savedItems.stream().map(item -> {
            Product product = productCatalog.get(item.getProductId());
            var stock = inventoryViewAssembler.productStock(item.getProductId(), session.getMarket());
            return new ArchiveItemData(item.getProductId(), product.displayName(), item.getSavedAt(),
                    item.getSavedAtStoreId(), item.getZoneId(), "", stock.homeMarket().status());
        }).toList();

        List<AdvisorBriefingData.UnresolvedItem> unresolved = intentService.currentSignal(sessionId)
                .filter(signal -> signal.unresolved() != UnresolvedCode.UNKNOWN)
                .map(signal -> {
                    String lastProductId = savedItems.isEmpty() ? null : savedItems.get(0).getProductId();
                    return List.of(new AdvisorBriefingData.UnresolvedItem(
                            signal.unresolved(), UNRESOLVED_LABELS.get(signal.unresolved()), lastProductId));
                })
                .orElse(List.of());

        String itemListText = savedItemData.stream()
                .map(item -> "- " + item.displayName())
                .reduce("", (a, b) -> a + b + "\n");
        String unresolvedListText = unresolved.stream()
                .map(AdvisorBriefingData.UnresolvedItem::label)
                .reduce("", (a, b) -> a + b + ", ");

        AdvisorBriefingData.Briefing briefing = aiBriefingService.summarize(
                itemListText, unresolvedListText, session.getMarket().name(), session.getLocale().value(),
                () -> fallbackBriefing(savedItemData, unresolved));

        // TODO(entry-P7): 실제 용어 사전이 붙기 전까지 keyPhrases는 비워둔다(AI-3 언어 자동 전환과 함께 후속 작업).
        return new AdvisorBriefingData(briefing, savedItemData, unresolved, session.getLocale(), List.of(), grant.getExpiresAt());
    }

    @Transactional
    public void addNote(String grantToken, String note) {
        AdvisorGrant grant = requireValidGrant(grantToken);
        noteRepository.save(new AdvisorNote(grant.getSessionId(), note, Instant.now()));
    }

    private AdvisorGrant requireValidGrant(String grantToken) {
        AdvisorGrant grant = grantRepository.findByGrantToken(grantToken)
                .orElseThrow(() -> EntryException.notFound("GRANT_NOT_FOUND", "열람 권한을 찾을 수 없습니다."));
        if (grant.isExpired()) {
            throw EntryException.unauthorized("GRANT_EXPIRED", "열람 권한이 만료되었습니다.");
        }
        return grant;
    }

    private AdvisorBriefingData.Briefing fallbackBriefing(List<ArchiveItemData> savedItems, List<AdvisorBriefingData.UnresolvedItem> unresolved) {
        if (savedItems.isEmpty()) {
            return new AdvisorBriefingData.Briefing("아직 저장한 제품이 없습니다.", false);
        }
        String names = savedItems.stream().map(ArchiveItemData::displayName).reduce((a, b) -> a + ", " + b).orElse("");
        String unresolvedPart = unresolved.isEmpty() ? "" : " " + unresolved.get(0).label() + "이 미해결 요인입니다.";
        return new AdvisorBriefingData.Briefing(names + "을 저장했습니다." + unresolvedPart, false);
    }
}
