package io.entry.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * seed/inventory.json을 메모리에 올려 조회하는 더미 구현.
 * 실 연동 시에는 이 클래스 대신 옴니채널 재고 API를 호출하는 InventoryPort 구현체를 주입한다.
 */
@Component
public class DummyInventoryAdapter implements InventoryPort {

    private final Map<String, List<InventoryRecord>> byProductId;

    public DummyInventoryAdapter(ObjectMapper objectMapper) {
        try {
            List<InventoryRecord> all = objectMapper.readValue(
                    new ClassPathResource("seed/inventory.json").getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, InventoryRecord.class));
            this.byProductId = all.stream().collect(Collectors.groupingBy(InventoryRecord::productId));
        } catch (IOException e) {
            throw new IllegalStateException("seed/inventory.json 로드 실패", e);
        }
    }

    @Override
    public List<InventoryRecord> byProduct(String productId) {
        return byProductId.getOrDefault(productId, List.of());
    }
}
