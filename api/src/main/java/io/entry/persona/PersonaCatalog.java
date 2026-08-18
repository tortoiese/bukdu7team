package io.entry.persona;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.common.EntryException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** seed/personas.json을 기동 시 1회 메모리에 올려두는 읽기 전용 카탈로그. */
@Component
public class PersonaCatalog {

    private final Map<String, Persona> byId;
    private final List<Persona> ordered;

    public PersonaCatalog(ObjectMapper objectMapper) {
        try {
            this.ordered = objectMapper.readValue(
                    new ClassPathResource("seed/personas.json").getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Persona.class));
            this.byId = ordered.stream().collect(Collectors.toMap(Persona::id, p -> p));
        } catch (IOException e) {
            throw new IllegalStateException("seed/personas.json 로드 실패", e);
        }
    }

    public Persona get(String id) {
        Persona persona = byId.get(id);
        if (persona == null) {
            throw EntryException.notFound("PERSONA_NOT_FOUND", "페르소나를 찾을 수 없습니다.");
        }
        return persona;
    }

    public List<Persona> all() {
        return ordered;
    }
}
