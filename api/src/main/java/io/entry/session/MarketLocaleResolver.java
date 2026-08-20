package io.entry.session;

import io.entry.common.AppLocale;
import io.entry.common.Market;
import org.springframework.stereotype.Component;

/**
 * Accept-Language + timezone으로 market/locale을 추정한다(CLAUDE.md 5장).
 * 시간대가 더 신뢰도 높은 신호이므로 우선 확인하고, 없으면 언어 태그로 폴백한다.
 */
@Component
public class MarketLocaleResolver {

    public record Inference(Market market, AppLocale locale) {
    }

    public Inference infer(String acceptLanguage, String timezone) {
        String tz = timezone == null ? "" : timezone;
        if (tz.contains("Hong_Kong")) return new Inference(Market.HK, AppLocale.ZH_HANT);
        if (tz.contains("Tokyo")) return new Inference(Market.JP, AppLocale.JA);
        if (tz.contains("Seoul")) return new Inference(Market.KR, AppLocale.KO);
        if (tz.contains("Singapore")) return new Inference(Market.SG, AppLocale.EN);
        if (isUsTimezone(tz)) return new Inference(Market.US, AppLocale.EN);

        String lang = acceptLanguage == null ? "" : acceptLanguage.toLowerCase();
        if (lang.startsWith("zh")) return new Inference(Market.HK, AppLocale.ZH_HANT);
        if (lang.startsWith("ja")) return new Inference(Market.JP, AppLocale.JA);
        if (lang.startsWith("en")) return new Inference(Market.US, AppLocale.EN);

        return new Inference(Market.KR, AppLocale.KO);
    }

    private boolean isUsTimezone(String tz) {
        return tz.startsWith("America/");
    }
}
