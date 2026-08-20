package io.entry.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.common.EntryException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ZoneCatalog {

    private final Map<String, Zone> byId;
    private final List<Zone> ordered;

    public ZoneCatalog(ObjectMapper objectMapper) {
        try {
            this.ordered = objectMapper.readValue(
                    new ClassPathResource("seed/zones.json").getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Zone.class));
            this.byId = ordered.stream().collect(Collectors.toMap(Zone::zoneId, z -> z));
        } catch (IOException e) {
            throw new IllegalStateException("seed/zones.json 로드 실패", e);
        }
    }

    public Zone get(String zoneId) {
        return byId.get(zoneId);
    }

    public Zone require(String zoneId) {
        String normalized = zoneId == null ? null : zoneId.trim().toUpperCase(Locale.ROOT);
        Zone zone = byId.get(normalized);
        if (zone == null) {
            throw EntryException.badRequest("INVALID_ZONE", "존 코드를 확인해주세요.");
        }
        return zone;
    }

    public List<Zone> all() {
        return ordered;
    }
}
