package io.entry.intent;

import io.entry.catalog.Product;
import io.entry.catalog.ProductCatalog;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 규칙 기반 의도 해석. AI 붙기 전까지(Phase 5 이전)의 기본 구현이자, AI 실패 시 폴백으로 계속 쓰인다.
 * rationale은 관측 사실만 서술한다 — 추측이나 감정적 표현을 넣지 않는다.
 */
@Component
public class RuleIntentAnalyzer {

    private final ProductCatalog productCatalog;

    public RuleIntentAnalyzer(ProductCatalog productCatalog) {
        this.productCatalog = productCatalog;
    }

    public IntentSignal analyze(List<String> scannedProductIdsInOrder, String currentProductId) {
        Product current = productCatalog.get(currentProductId);

        Set<String> distinctLines = new LinkedHashSet<>();
        int sameLineCount = 0;
        boolean sameLineSizeVaries = false;
        String firstSameLineSize = null;

        for (String pid : scannedProductIdsInOrder) {
            Product p = productCatalog.get(pid);
            distinctLines.add(p.line());
            if (p.line().equals(current.line())) {
                sameLineCount++;
                if (firstSameLineSize == null) {
                    firstSameLineSize = p.sizeOrigin();
                } else if (!firstSameLineSize.equals(p.sizeOrigin())) {
                    sameLineSizeVaries = true;
                }
            }
        }

        long rescanCount = scannedProductIdsInOrder.stream().filter(currentProductId::equals).count();
        boolean rescanned = rescanCount >= 2;

        IntentStage stage;
        String axis;
        UnresolvedCode unresolved;
        String rationale;

        if (distinctLines.size() > 1) {
            stage = IntentStage.CATEGORY_COMPARE;
            axis = "LINE";
            unresolved = UnresolvedCode.UNKNOWN;
            rationale = "서로 다른 라인을 오가며 " + scannedProductIdsInOrder.size() + "회 스캔했습니다.";
        } else if (sameLineCount >= 3 && !sameLineSizeVaries) {
            stage = IntentStage.SIZE_DECIDED;
            axis = "COLOR";
            unresolved = UnresolvedCode.COLOR_CARE;
            rationale = "같은 라인 안에서 컬러만 바꿔 " + sameLineCount + "회 스캔했습니다.";
        } else if (sameLineCount >= 2 && sameLineSizeVaries) {
            stage = IntentStage.LINE_COMPARE;
            axis = "SIZE";
            unresolved = UnresolvedCode.SIZE;
            rationale = "같은 라인 안에서 사이즈를 바꿔가며 " + sameLineCount + "회 스캔했습니다.";
        } else if (sameLineCount >= 2) {
            stage = IntentStage.LINE_COMPARE;
            axis = "COLOR";
            unresolved = UnresolvedCode.COLOR_CARE;
            rationale = "같은 라인 안에서 " + sameLineCount + "회 스캔했습니다.";
        } else {
            stage = IntentStage.BROWSING;
            axis = "NONE";
            unresolved = UnresolvedCode.UNKNOWN;
            rationale = "이 제품을 처음 살펴보고 있습니다.";
        }

        double confidence = 0.5;
        if (stage == IntentStage.SIZE_DECIDED) confidence = 0.65;
        if (rescanned) {
            confidence += 0.2;
            rationale += " 같은 제품을 다시 확인했습니다.";
        }
        confidence = Math.min(confidence, 0.95);

        return new IntentSignal(stage, axis, unresolved, confidence, rationale, false);
    }
}
