package io.entry.common;

public final class CurrencyByMarket {

    private CurrencyByMarket() {
    }

    public static String of(Market market) {
        return switch (market) {
            case KR -> "KRW";
            case HK -> "HKD";
            case JP -> "JPY";
            case US -> "USD";
        };
    }
}
