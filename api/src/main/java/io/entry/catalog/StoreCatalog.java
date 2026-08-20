package io.entry.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.common.BrandProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StoreCatalog {

    private final Map<String, Store> byId;
    private final List<Store> ordered;
    private final BrandProperties brandProperties;

    public StoreCatalog(ObjectMapper objectMapper, BrandProperties brandProperties) {
        this.brandProperties = brandProperties;
        try {
            this.ordered = objectMapper.readValue(
                    new ClassPathResource("seed/stores.json").getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Store.class));
            this.byId = ordered.stream().collect(Collectors.toMap(Store::storeId, s -> s));
        } catch (IOException e) {
            throw new IllegalStateException("seed/stores.json 로드 실패", e);
        }
    }

    /** storeId가 카탈로그에 없거나 비어 있으면(기존 QR 호환) 기본 매장(BrandProperties)으로 폴백한다. */
    public Store getOrDefault(String storeId) {
        Store found = storeId == null ? null : byId.get(storeId);
        if (found != null) return found;
        return new Store(brandProperties.getOriginStoreId(), brandProperties.getOriginStoreName(),
                brandProperties.getIssuedPlace(), brandProperties.getPopupId());
    }

    public List<Store> all() {
        return ordered;
    }
}
