package io.entry.ai;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * resources/prompts/*.md 를 읽어 캐시한다. 프롬프트는 자바 문자열이 아니라 이 디렉터리에 둔다(CLAUDE.md 6장).
 */
@Component
public class PromptLoader {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String load(String name) {
        return cache.computeIfAbsent(name, this::readFile);
    }

    private String readFile(String name) {
        try {
            byte[] bytes = new ClassPathResource("prompts/" + name + ".md").getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("프롬프트 파일을 읽지 못했습니다: " + name, e);
        }
    }
}
