package io.entry.advisor.dto;

import io.entry.archive.dto.ArchiveItemData;
import io.entry.common.AppLocale;
import io.entry.intent.UnresolvedCode;

import java.time.Instant;
import java.util.List;

public record AdvisorBriefingData(
        Briefing briefing,
        List<ArchiveItemData> savedItems,
        List<UnresolvedItem> unresolved,
        AppLocale locale,
        List<KeyPhrase> keyPhrases,
        Instant expiresAt
) {
    public record Briefing(String text, boolean aiUsed) {
    }

    public record UnresolvedItem(UnresolvedCode code, String label, String productId) {
    }

    public record KeyPhrase(String ko, String target) {
    }
}
