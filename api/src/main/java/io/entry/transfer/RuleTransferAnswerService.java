package io.entry.transfer;

import io.entry.intent.UnresolvedCode;
import io.entry.transfer.dto.TransferData;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** Phase 5에서 AI(transfer-message)가 붙기 전까지의 기본 응답. AI 실패 시 폴백으로도 쓰인다. */
@Component
public class RuleTransferAnswerService {

    private record QA(String question, String answer) {
    }

    private static final Map<UnresolvedCode, QA> ANSWERS = Map.of(
            UnresolvedCode.SIZE, new QA("사이즈가 맞을지 확신이 서지 않았습니다.", "현지 매장에서 실측 후 결정하실 수 있습니다."),
            UnresolvedCode.COLOR_CARE, new QA("밝은 컬러 관리가 어렵지 않을까 고민이 있었습니다.", "코팅 소재는 물티슈로 닦아내는 방식으로 관리할 수 있습니다."),
            UnresolvedCode.PORTABILITY, new QA("휴대하기에 무겁지 않을지 궁금했습니다.", "제품 상세의 무게 정보로 휴대성을 다시 비교해보실 수 있습니다."),
            UnresolvedCode.CAPACITY, new QA("수납 공간이 충분할지 궁금했습니다.", "노트북과 보조 소지품을 함께 넣을 수 있는 폭으로 제작되었습니다."),
            UnresolvedCode.GIFT_FIT, new QA("선물로 적합할지 확신이 서지 않았습니다.", "라인 인지도가 높아 무난한 선물로 선택하는 분들이 많습니다.")
    );

    public Optional<TransferData.UnresolvedAnswer> answerFor(UnresolvedCode code) {
        QA qa = ANSWERS.get(code);
        if (qa == null) return Optional.empty();
        return Optional.of(new TransferData.UnresolvedAnswer(code, qa.question(), qa.answer(), false));
    }

    /** AiTransferAnswerService가 프롬프트에 넣을 "관측된 질문" 문구를 여기서 재사용한다. */
    public Optional<String> questionFor(UnresolvedCode code) {
        QA qa = ANSWERS.get(code);
        return qa == null ? Optional.empty() : Optional.of(qa.question());
    }
}
