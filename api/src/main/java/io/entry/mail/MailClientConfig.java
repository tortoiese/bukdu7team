package io.entry.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * io.entry.ai.AiClientConfig와 동일한 패턴. entry.mail.mock(기본값 true)이면 MockMailClient,
 * false면 ResendMailClient를 빈으로 등록한다.
 */
@Configuration
public class MailClientConfig {

    @Bean
    public MailClient mailClient(WebClient.Builder webClientBuilder, MailProperties properties,
                                  @Value("${RESEND_API_KEY:}") String resendApiKey) {
        if (properties.isMock()) {
            return new MockMailClient();
        }
        return new ResendMailClient(webClientBuilder, properties, resendApiKey);
    }
}
