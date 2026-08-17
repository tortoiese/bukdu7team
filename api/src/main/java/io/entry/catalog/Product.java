package io.entry.catalog;

import java.util.List;

/**
 * 제품 카탈로그 항목. products.json에서 그대로 로드되는 읽기 전용 참조 데이터다.
 * 가격 필드는 절대 추가하지 않는다(CLAUDE.md R2).
 */
public record Product(
        String productId,
        String line,
        String displayName,
        String material,
        int weightGram,
        String sizeLocal,
        String sizeOrigin,
        List<CraftNote> craftNotes
) {
}
