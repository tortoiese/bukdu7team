package io.entry.scan;

import io.entry.catalog.Product;
import io.entry.common.CharacterId;
import io.entry.intent.IntentSignal;
import io.entry.scan.dto.GreetingData;
import org.springframework.stereotype.Component;

/**
 * 규칙 기반 인사 생성. Phase 5에서 AI(AI-2)가 붙기 전까지의 기본 구현이자, AI 실패 시 폴백이다.
 * 구매를 권유하지 않고 결정 기준을 묻는 질문으로 끝낸다.
 */
@Component
public class RuleGreetingService {

    private static final CharacterId[] ROTATION = {CharacterId.HARU, CharacterId.HENRY, CharacterId.KAISER};

    /** 제품ID로부터 결정론적으로 캐릭터를 고른다 — 같은 제품이면 항상 같은 캐릭터가 응대한다. */
    public static CharacterId characterFor(Product product) {
        return ROTATION[Math.floorMod(product.productId().hashCode(), ROTATION.length)];
    }

    public GreetingData greet(Product product, IntentSignal intent, int scanCountForProduct) {
        CharacterId character = characterFor(product);

        String ordinal = scanCountForProduct >= 3 ? "세 번째" : scanCountForProduct == 2 ? "두 번째" : "";
        String question = switch (intent.unresolved()) {
            case SIZE -> "사이즈 때문인가요?";
            case COLOR_CARE -> "컬러 관리가 궁금하신가요?";
            case PORTABILITY -> "휴대성 때문인가요?";
            case CAPACITY -> "수납량 때문인가요?";
            case GIFT_FIT -> "선물용으로 고민 중이신가요?";
            case UNKNOWN -> "어떤 점이 궁금하신가요?";
        };

        String message = scanCountForProduct >= 2
                ? String.format("%s, %s 보시네요. %s", product.displayName(), ordinal, question)
                : String.format("%s입니다. %s", product.displayName(), question);

        return new GreetingData(character, message);
    }
}
