package io.entry.mail;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Resend(https://resend.com) REST API 호출. RESEND_API_KEY가 없으면 발송을 시도하지 않고
 * 바로 실패시킨다 — 프론트에는 절대 키를 노출하지 않고 이 백엔드 클라이언트만 호출한다(CLAUDE.md R6와 동일한 원칙).
 */
public class ResendMailClient implements MailClient {

    private final WebClient webClient;
    private final MailProperties properties;
    private final String apiKey;

    public ResendMailClient(WebClient.Builder webClientBuilder, MailProperties properties, String apiKey) {
        this.webClient = webClientBuilder.baseUrl("https://api.resend.com").build();
        this.properties = properties;
        this.apiKey = apiKey;
    }

    @Override
    public void send(String to, String subject, String bodyText) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new MailUnavailableException("RESEND_API_KEY가 설정되지 않았습니다.");
        }

        Map<String, Object> body = Map.of(
                "from", properties.getFromAddress(),
                "to", List.of(to),
                "subject", subject,
                "text", bodyText
        );

        try {
            JsonNode response = webClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(8))
                    .retryWhen(Retry.max(1))
                    .block();

            if (response == null || !response.has("id")) {
                throw new MailUnavailableException("메일 발송 응답이 예상과 다릅니다.");
            }
        } catch (MailUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new MailUnavailableException("메일 발송 실패", e);
        }
    }
}
