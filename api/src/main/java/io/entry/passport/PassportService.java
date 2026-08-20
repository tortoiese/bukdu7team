package io.entry.passport;

import io.entry.archive.SavedItemRepository;
import io.entry.catalog.Store;
import io.entry.catalog.StoreCatalog;
import io.entry.catalog.Zone;
import io.entry.catalog.ZoneCatalog;
import io.entry.common.EntryException;
import io.entry.common.MrzBuilder;
import io.entry.intent.IntentService;
import io.entry.passport.dto.PassportData;
import io.entry.passport.dto.StampResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PassportService {

    private static final Duration MIN_STAMP_INTERVAL = Duration.ofSeconds(60);
    private final Random random = new Random();

    private final PassportRepository passportRepository;
    private final PassportStampRepository stampRepository;
    private final SavedItemRepository savedItemRepository;
    private final ZoneCatalog zoneCatalog;
    private final StoreCatalog storeCatalog;
    private final PassportTier passportTier;
    private final IntentService intentService;

    public PassportService(PassportRepository passportRepository, PassportStampRepository stampRepository,
                            SavedItemRepository savedItemRepository, ZoneCatalog zoneCatalog, StoreCatalog storeCatalog,
                            PassportTier passportTier, IntentService intentService) {
        this.passportRepository = passportRepository;
        this.stampRepository = stampRepository;
        this.savedItemRepository = savedItemRepository;
        this.zoneCatalog = zoneCatalog;
        this.storeCatalog = storeCatalog;
        this.passportTier = passportTier;
        this.intentService = intentService;
    }

    @Transactional
    public PassportData issue(UUID sessionId, String storeId) {
        Passport passport = issueOrGet(sessionId, storeId);
        return toData(passport);
    }

    /**
     * 실제 게스트 흐름에서는 "발급" 버튼이 따로 없다 — 제품 태그든 구역 태그든
     * 첫 스캔이 곧 입구 태그를 댄 것과 같은 의미라 그 순간 조용히 발급된다(멱등).
     * 어느 매장(성수/더현대/합정/문래) QR로 들어왔는지에 따라 발급 정보가 갈린다 — 이미 발급된
     * 세션이면 storeId를 무시하고 기존 패스포트를 그대로 돌려준다(최초 매장이 우선).
     * ScanService(P1)·stamp(P4 구역 검인) 양쪽에서 호출한다.
     */
    @Transactional
    public Passport issueOrGet(UUID sessionId, String storeId) {
        return passportRepository.findBySessionId(sessionId).orElseGet(() -> {
            Store store = storeCatalog.getOrDefault(storeId);
            String passportNo = String.format("ENT-KR-%07d", passportRepository.count() + 1);
            Passport created = new Passport(sessionId, passportNo, store.popupId(), store.issuedPlace(), Instant.now());
            return passportRepository.save(created);
        });
    }

    public PassportData get(UUID sessionId) {
        Passport passport = passportRepository.findBySessionId(sessionId)
                .orElseThrow(() -> EntryException.notFound("PASSPORT_NOT_FOUND", "패스포트가 아직 발급되지 않았습니다."));
        return toData(passport);
    }

    @Transactional
    public StampResponse stamp(UUID sessionId, String zoneId, String storeId) {
        Passport passport = issueOrGet(sessionId, storeId);

        if (stampRepository.findByPassportIdAndZoneId(passport.getId(), zoneId).isPresent()) {
            throw EntryException.conflict("ZONE_ALREADY_STAMPED", "이미 검인된 구역입니다.");
        }

        stampRepository.findTopByPassportIdOrderByStampedAtDesc(passport.getId()).ifPresent(last -> {
            if (Duration.between(last.getStampedAt(), Instant.now()).compareTo(MIN_STAMP_INTERVAL) < 0) {
                throw EntryException.tooManyRequests("STAMP_TOO_SOON", "검인 간 최소 60초 간격이 필요합니다.");
            }
        });

        int rotationSeed = random.nextInt(100);
        PassportStamp stamp = stampRepository.save(new PassportStamp(passport.getId(), zoneId, Instant.now(), rotationSeed));

        List<PassportStamp> stamps = stampRepository.findByPassportIdOrderByStampedAtAsc(passport.getId());
        Set<String> visitedZoneIds = stamps.stream().map(PassportStamp::getZoneId).collect(Collectors.toSet());

        int previousTier = passportTier.tierFor(visitedZoneIds.size() - 1);
        int currentTier = passportTier.tierFor(visitedZoneIds.size());

        return new StampResponse(
                zoneId, stamp.getStampedAt(), rotationSeed, currentTier,
                currentTier > previousTier,
                passportTier.nextTierFor(currentTier, visitedZoneIds));
    }

    private PassportData toData(Passport passport) {
        List<PassportStamp> stamps = stampRepository.findByPassportIdOrderByStampedAtAsc(passport.getId());
        Set<String> visitedZoneIds = stamps.stream().map(PassportStamp::getZoneId).collect(Collectors.toSet());

        List<PassportData.ZoneEntry> zoneEntries = zoneCatalog.all().stream()
                .map(zone -> toZoneEntry(zone, stamps))
                .toList();

        long savedCount = savedItemRepository.countBySessionId(passport.getSessionId());
        int tier = passportTier.tierFor(visitedZoneIds.size());

        String lastZoneId = stamps.isEmpty() ? null : stamps.get(stamps.size() - 1).getZoneId();
        String comparisonAxis = intentService.currentSignal(passport.getSessionId())
                .map(signal -> signal.comparisonAxis())
                .orElse(null);

        String[] mrz = MrzBuilder.build(lastZoneId, savedCount, comparisonAxis,
                io.entry.common.Market.KR, null);

        return new PassportData(
                passport.getPassportNo(), passport.getIssuedAt(), passport.getIssuedPlace(), passport.getPopupId(),
                zoneEntries, savedCount, tier, passportTier.grantsFor(tier), mrz);
    }

    private PassportData.ZoneEntry toZoneEntry(Zone zone, List<PassportStamp> stamps) {
        Optional<PassportStamp> stamp = stamps.stream().filter(s -> s.getZoneId().equals(zone.zoneId())).findFirst();
        return new PassportData.ZoneEntry(
                zone.zoneId(), zone.name(), stamp.isPresent(),
                stamp.map(PassportStamp::getStampedAt).orElse(null),
                stamp.map(PassportStamp::getRotationSeed).orElse(null));
    }
}
