package io.entry.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * AiClient 빈을 entry.ai.mock / entry.ai.provider 값에 따라 고른다.
 * 기본값은 mock=true — API 키 없이도 전 화면이 폴백/목업 응답으로 정상 동작해야 한다.
 * mock=false일 때만 provider(anthropic 기본값 | openai)로 실제 호출 대상을 정한다.
 */
@Configuration
public class AiClientConfig {

    @Bean
    public AiClient aiClient(WebClient.Builder webClientBuilder, AiProperties properties,
                              @Value("${ANTHROPIC_API_KEY:}") String anthropicApiKey,
                              @Value("${OPENAI_API_KEY:}") String openAiApiKey) {
        if (properties.isMock()) {
            return new MockAiClient();
        }
        return switch (properties.getProvider()) {
            case OPENAI -> new OpenAiClient(webClientBuilder, properties, openAiApiKey);
            case ANTHROPIC -> new AnthropicClient(webClientBuilder, properties, anthropicApiKey);
        };
    }
}
