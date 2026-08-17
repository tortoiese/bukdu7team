package io.entry.archive.dto;

import java.util.List;

public record ArchiveListData(List<ArchiveItemData> items, IntentSummary intentSummary) {
}
