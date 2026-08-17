package io.entry.passport;

import io.entry.catalog.Zone;
import io.entry.catalog.ZoneCatalog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 방문 구역 수로 열람 권한 단계를 계산한다. 보상은 언제나 "접근 권한" 표현만 쓴다(CLAUDE.md R2).
 */
@Component
public class PassportTier {

    public record Grant(String code, String label, boolean active) {
    }

    public record NextTier(int tier, String requirement, List<String> remainingZones) {
    }

    private final ZoneCatalog zoneCatalog;

    public PassportTier(ZoneCatalog zoneCatalog) {
        this.zoneCatalog = zoneCatalog;
    }

    public int tierFor(int visitedCount) {
        int totalZones = zoneCatalog.all().size();
        if (visitedCount <= 0) return 1;
        if (visitedCount >= totalZones) return 4;
        return 1 + (int) Math.ceil(visitedCount * 3.0 / totalZones);
    }

    public List<Grant> grantsFor(int tier) {
        return List.of(
                new Grant("PRIORITY_ENTRY", "전용 시간대 우선 입장", tier >= 1),
                new Grant("ARCHIVE_VIEW", "팝업 한정 아카이브 열람", tier >= 2),
                new Grant("EARLY_ACCESS", "선공개 알림", tier >= 3),
                new Grant("NEXT_POPUP_INVITE", "다음 팝업 초대", tier >= 4)
        );
    }

    public NextTier nextTierFor(int tier, Set<String> visitedZoneIds) {
        if (tier >= 4) return null;

        List<String> remaining = zoneCatalog.all().stream()
                .map(Zone::zoneId)
                .filter(id -> !visitedZoneIds.contains(id))
                .toList();

        String requirement = tier == 3 ? "전 구역 방문" : "구역 " + remaining.size() + "곳 더 방문";
        return new NextTier(tier + 1, requirement, remaining);
    }
}
