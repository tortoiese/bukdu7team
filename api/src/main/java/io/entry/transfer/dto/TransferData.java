package io.entry.transfer.dto;

import io.entry.common.Market;
import io.entry.common.StockStatus;
import io.entry.intent.UnresolvedCode;

import java.time.Instant;
import java.util.List;

public record TransferData(
        String originStore,
        Market targetMarket,
        String currency,
        Instant generatedAt,
        SendTiming sendTiming,
        List<Item> items,
        List<UnresolvedAnswer> unresolvedAnswers,
        MrzTransition mrzTransition
) {
    public record SendTiming(Instant recommendedAt, String rationale, boolean aiUsed) {
    }

    public record Action(String type, String label, String url) {
    }

    public record Item(
            String productId, String displayName, StockStatus status,
            String storeName, Integer transferDays, String fromStore, Action action
    ) {
    }

    public record UnresolvedAnswer(UnresolvedCode code, String question, String answer, boolean aiUsed) {
    }

    public record MrzTransition(String from, String to) {
    }
}
