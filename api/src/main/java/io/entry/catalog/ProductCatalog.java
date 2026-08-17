package io.entry.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.common.EntryException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * seed/products.json을 기동 시 1회 메모리에 올려두는 읽기 전용 카탈로그.
 * 제품 목록은 세션마다 바뀌지 않는 참조 데이터라 DB 왕복 없이 서빙한다.
 */
@Component
public class ProductCatalog {

    private final Map<String, Product> byId;

    public ProductCatalog(ObjectMapper objectMapper) {
        try {
            List<Product> products = objectMapper.readValue(
                    new ClassPathResource("seed/products.json").getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class));
            this.byId = products.stream().collect(Collectors.toMap(Product::productId, p -> p));
        } catch (IOException e) {
            throw new IllegalStateException("seed/products.json 로드 실패", e);
        }
    }

    public Product get(String productId) {
        Product product = byId.get(productId);
        if (product == null) {
            throw EntryException.notFound("PRODUCT_NOT_FOUND", "제품을 찾을 수 없습니다.");
        }
        return product;
    }

    public List<Product> all() {
        return List.copyOf(byId.values());
    }
}
