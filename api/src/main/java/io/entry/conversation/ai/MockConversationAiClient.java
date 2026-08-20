package io.entry.conversation.ai;

import io.entry.intent.UnresolvedCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "entry.ai.mock", havingValue = "true", matchIfMissing = true)
public class MockConversationAiClient implements ConversationAiClient {

    @Override
    public AiConversationReply generate(AiConversationContext context) {
        String message = switch (context.locale()) {
            case EN -> "Let's clarify your most important criterion first. Which should we check first: color, size, or weight?";
            case JA -> "まず最も大切な判断基準を整理しましょう。カラー、サイズ、重さのどれから確認しますか？";
            case ZH_HANT -> "先整理您最重視的判斷標準。想先確認顏色、尺寸還是重量？";
            case KO -> "지금 가장 중요한 결정 기준을 한 가지씩 정리해볼게요. 컬러, 크기, 무게 중 어디부터 확인할까요?";
        };
        return new AiConversationReply(
                message,
                List.of(),
                UnresolvedCode.UNKNOWN,
                false,
                false
        );
    }
}
