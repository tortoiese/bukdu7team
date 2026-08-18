package io.entry.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * entry.ai.mock 값에 따라 AiClient 빈을 AnthropicClient 또는 MockAiClient 중 하나로 등록한다.
 * 기본값은 mock=true — ANTHROPIC_API_KEY 없이도 전 화면이 폴백/목업 응답으로 정상 동작해야 한다.
 */
@Configuration
public class AiClientConfig {

    @Bean
    @ConditionalOnProperty(prefix = "entry.ai", name = "mock", havingValue = "false")
    public AiClient anthropicAiClient(WebClient.Builder webClientBuilder, AiProperties properties,
                                       @Value("${ANTHROPIC_API_KEY:}") String apiKey) {
        return new AnthropicClient(webClientBuilder, properties, apiKey);
    }

    @Bean
    @ConditionalOnProperty(prefix = "entry.ai", name = "mock", havingValue = "true", matchIfMissing = true)
    public AiClient mockAiClient() {
        return new MockAiClient();
    }
}
