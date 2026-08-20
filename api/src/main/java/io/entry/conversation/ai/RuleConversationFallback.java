package io.entry.conversation.ai;

import io.entry.intent.UnresolvedCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class RuleConversationFallback {

    public AiConversationReply generate(AiConversationContext context) {
        UnresolvedCode unresolved = unresolved(context);
        return new AiConversationReply(
                answer(context, unresolved),
                criteria(context, unresolved),
                unresolved,
                false,
                true
        );
    }

    private String answer(AiConversationContext context, UnresolvedCode unresolved) {
        return switch (context.locale()) {
            case KO -> answerKo(context, unresolved);
            case EN -> answerEn(context, unresolved);
            case JA -> answerJa(context, unresolved);
            case ZH_HANT -> answerZh(context, unresolved);
        };
    }

    private String answerKo(AiConversationContext context, UnresolvedCode unresolved) {
        return switch (unresolved) {
            case COLOR_CARE -> "%s 소재의 관리 방법을 기준으로 비교해보세요. 오염은 부드러운 천으로 가병게 닦는 방식을 권합니다."
                    .formatted(context.product().material());
            case SIZE -> "표기 사이즈는 %s(%s)입니다. 소지품 크기와 매장에서의 착용감을 함께 확인해보세요."
                    .formatted(context.product().sizeLocal(), context.product().sizeOrigin());
            case PORTABILITY -> "등록된 무게는 %dg입니다. 실제 체감은 수납량에 따라 달라질 수 있어요."
                    .formatted(context.product().weightGram());
            case CAPACITY -> "수납 여부는 가지고 다니는 물건의 실제 크기를 기준으로 확인하는 것이 정확합니다.";
            case GIFT_FIT -> "선물 받을 분의 사용 장면과 선호하는 크기를 기준으로 정리해보세요.";
            case UNKNOWN -> "결정을 망설이게 하는 기준이 컬러, 크기, 무게 중 무엇인지부터 정리해볼까요?";
        };
    }

    private String answerEn(AiConversationContext context, UnresolvedCode unresolved) {
        return switch (unresolved) {
            case COLOR_CARE -> "Compare the care requirements of the %s material. For light marks, use a soft cloth without rubbing hard."
                    .formatted(context.product().material());
            case SIZE -> "The listed size is %s (%s). Compare it with what you carry and check the fit in store."
                    .formatted(context.product().sizeLocal(), context.product().sizeOrigin());
            case PORTABILITY -> "The registered weight is %dg. How it feels can change depending on how much you carry."
                    .formatted(context.product().weightGram());
            case CAPACITY -> "Check capacity against the actual dimensions of the items you carry most often.";
            case GIFT_FIT -> "Consider how the recipient will use it and which size they usually prefer.";
            case UNKNOWN -> "Which criterion is holding back your decision most: color, size, or weight?";
        };
    }

    private String answerJa(AiConversationContext context, UnresolvedCode unresolved) {
        return switch (unresolved) {
            case COLOR_CARE -> "%s素材のお手入れ方法を基準に比較してください。軽い汚れは柔らかい布で優しく拭くのがおすすめです。"
                    .formatted(context.product().material());
            case SIZE -> "表記サイズは%s（%s）です。持ち物の大きさと店頭でのフィット感を確認してください。"
                    .formatted(context.product().sizeLocal(), context.product().sizeOrigin());
            case PORTABILITY -> "登録重量は%dgです。実際の感じ方は収納量によって変わります。"
                    .formatted(context.product().weightGram());
            case CAPACITY -> "よく持ち歩く物の実際のサイズを基準に収納力を確認してください。";
            case GIFT_FIT -> "贈る相手の使用シーンと好みのサイズを基準に考えましょう。";
            case UNKNOWN -> "カラー、サイズ、重さのうち、どの基準が一番気になりますか？";
        };
    }

    private String answerZh(AiConversationContext context, UnresolvedCode unresolved) {
        return switch (unresolved) {
            case COLOR_CARE -> "可依據%s材質的保養方式進行比較。輕微污漬建議用柔軟的布輕輕擦拭。"
                    .formatted(context.product().material());
            case SIZE -> "標示尺寸為%s（%s）。建議對照隨身物品大小，並在店內確認背起來的感受。"
                    .formatted(context.product().sizeLocal(), context.product().sizeOrigin());
            case PORTABILITY -> "登錄重量為%dg。實際感受會隨收納量而改變。"
                    .formatted(context.product().weightGram());
            case CAPACITY -> "請以平常攜帶物品的實際尺寸確認收納空間。";
            case GIFT_FIT -> "可以送禮對象的使用情境與常用尺寸作為判斷標準。";
            case UNKNOWN -> "顏色、尺寸與重量之中，哪一項最影響您的決定？";
        };
    }

    private UnresolvedCode unresolved(AiConversationContext context) {
        String text = context.userText().toLowerCase(Locale.ROOT);
        return switch (context.locale()) {
            case KO -> classify(text,
                    List.of("색", "컬러", "관리", "오염"), List.of("사이즈", "크기", "맞"),
                    List.of("무게", "무겁", "휴대"), List.of("수납", "노트북", "들어"), List.of("선물"));
            case EN -> classify(text,
                    List.of("color", "colour", "care", "stain"), List.of("size", "fit"),
                    List.of("weight", "heavy", "carry", "portable"), List.of("capacity", "laptop", "hold"), List.of("gift"));
            case JA -> classify(text,
                    List.of("色", "カラー", "手入れ", "汚れ"), List.of("サイズ", "大きさ", "合う"),
                    List.of("重さ", "重い", "持ち運び"), List.of("収納", "ノートパソコン", "入る"),
                    List.of("ギフト", "プレゼント"));
            case ZH_HANT -> classify(text,
                    List.of("顏色", "颜色", "保養", "保养", "污漬", "污渍"),
                    List.of("尺寸", "大小", "合適", "合适"), List.of("重量", "重", "攜帶", "携带"),
                    List.of("收納", "筆電", "笔记本"), List.of("禮物", "礼物", "送禮", "送礼"));
        };
    }

    private UnresolvedCode classify(String text, List<String> color, List<String> size,
                                    List<String> portability, List<String> capacity, List<String> gift) {
        if (containsAny(text, color)) return UnresolvedCode.COLOR_CARE;
        if (containsAny(text, size)) return UnresolvedCode.SIZE;
        if (containsAny(text, portability)) return UnresolvedCode.PORTABILITY;
        if (containsAny(text, capacity)) return UnresolvedCode.CAPACITY;
        if (containsAny(text, gift)) return UnresolvedCode.GIFT_FIT;
        return UnresolvedCode.UNKNOWN;
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }

    private List<String> criteria(AiConversationContext context, UnresolvedCode unresolved) {
        if (unresolved == UnresolvedCode.UNKNOWN) return List.of();
        return switch (context.locale()) {
            case KO -> switch (unresolved) {
                case COLOR_CARE -> List.of("관리 용이성");
                case SIZE -> List.of("착용 사이즈");
                case PORTABILITY -> List.of("휴대성");
                case CAPACITY -> List.of("수납량");
                case GIFT_FIT -> List.of("선물 적합성");
                case UNKNOWN -> List.of();
            };
            case EN -> switch (unresolved) {
                case COLOR_CARE -> List.of("Ease of care");
                case SIZE -> List.of("Fit and size");
                case PORTABILITY -> List.of("Portability");
                case CAPACITY -> List.of("Capacity");
                case GIFT_FIT -> List.of("Gift suitability");
                case UNKNOWN -> List.of();
            };
            case JA -> List.of("判断基準");
            case ZH_HANT -> List.of("判斷標準");
        };
    }
}
