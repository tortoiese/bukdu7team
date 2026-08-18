package io.entry.admin.dto;

import io.entry.common.Market;
import io.entry.intent.UnresolvedCode;

import java.util.List;

public record IntentDashboardData(
        List<ProductStat> products,
        List<UnresolvedCount> unresolvedDistribution,
        List<MarketCount> marketDistribution,
        TransferRecovery transferRecovery,
        List<ZoneStat> zonePerformance,
        List<String> actionHints
) {
    public record ProductStat(String productId, long scans, double rescanRate, double saveRate, double conversionRate) {
    }

    public record UnresolvedCount(UnresolvedCode code, long count) {
    }

    public record MarketCount(Market market, long sessions) {
    }

    public record TransferRecovery(long sent, long converted, double rate) {
    }

    public record ZoneStat(String zoneId, long avgDwellSeconds, long saves) {
    }
}
