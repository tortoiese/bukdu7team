package io.entry.conversation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.common.AppLocale;
import io.entry.common.Market;
import io.entry.scan.ScanEvent;
import io.entry.scan.ScanEventRepository;
import io.entry.session.AnonymousSession;
import io.entry.session.SessionInterceptor;
import io.entry.session.SessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConversationIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired SessionRepository sessionRepository;
    @Autowired ScanEventRepository scanEventRepository;
    @Autowired ConversationService conversationService;
    @Autowired ConversationMessageRepository messageRepository;

    @Test
    void 같은_스캔의_대화_시작은_멱등적이다() throws Exception {
        Fixture fixture = fixture();

        String first = start(fixture.sessionId(), fixture.scanId());
        String second = start(fixture.sessionId(), fixture.scanId());

        assertThat(second).isEqualTo(first);
    }

    @Test
    void 같은_스캔으로_동시에_대화를_시작해도_하나만_반환한다() throws Exception {
        Fixture fixture = fixture();
        int requestCount = 6;
        CyclicBarrier barrier = new CyclicBarrier(requestCount);
        var executor = Executors.newFixedThreadPool(requestCount);

        try {
            var futures = java.util.stream.IntStream.range(0, requestCount)
                    .mapToObj(ignored -> executor.submit(() -> {
                        barrier.await();
                        return conversationService.start(fixture.sessionId(), fixture.scanId()).conversationId();
                    }))
                    .toList();

            Set<UUID> conversationIds = futures.stream()
                    .map(future -> {
                        try {
                            return future.get(10, TimeUnit.SECONDS);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                    .collect(java.util.stream.Collectors.toSet());

            assertThat(conversationIds).hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void 다른_세션의_스캔으로_대화를_시작할_수_없다() throws Exception {
        Fixture fixture = fixture();
        UUID otherSession = createSession();

        mvc.perform(post("/api/v1/conversations")
                        .header(SessionInterceptor.HEADER, otherSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scanId\":\"%s\"}".formatted(fixture.scanId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SCAN_NOT_FOUND"));
    }

    @Test
    void 세_턴_후에는_직원_연결을_제안한다() throws Exception {
        Fixture fixture = fixture();
        String conversationId = start(fixture.sessionId(), fixture.scanId());

        for (int turn = 1; turn <= 3; turn++) {
            mvc.perform(post("/api/v1/conversations/{id}/messages", conversationId)
                            .header(SessionInterceptor.HEADER, fixture.sessionId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"text\":\"%d번째 고민입니다\"}".formatted(turn)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.turnsRemaining").value(3 - turn));
        }

        mvc.perform(post("/api/v1/conversations/{id}/messages", conversationId)
                        .header(SessionInterceptor.HEADER, fixture.sessionId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"추가 질문입니다\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.turnsRemaining").value(0))
                .andExpect(jsonPath("$.data.handoffSuggested").value(true))
                .andExpect(jsonPath("$.meta.fallback").value(false));
    }

    @Test
    void 동시_메시지가_들어와도_사용자_대화는_세_턴만_저장한다() throws Exception {
        Fixture fixture = fixture();
        UUID conversationId = UUID.fromString(start(fixture.sessionId(), fixture.scanId()));
        int requestCount = 6;
        CyclicBarrier barrier = new CyclicBarrier(requestCount);
        var executor = Executors.newFixedThreadPool(requestCount);

        try {
            var futures = java.util.stream.IntStream.range(0, requestCount)
                    .mapToObj(index -> executor.submit(() -> {
                        barrier.await();
                        return conversationService.reply(fixture.sessionId(), conversationId, index + "번 질문");
                    }))
                    .toList();
            for (var future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }

            long userMessageCount = messageRepository
                    .findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                    .filter(message -> "USER".equals(message.getRole()))
                    .count();
            assertThat(userMessageCount).isEqualTo(3);
        } finally {
            executor.shutdownNow();
        }
    }

    private String start(UUID sessionId, UUID scanId) throws Exception {
        String response = mvc.perform(post("/api/v1/conversations")
                        .header(SessionInterceptor.HEADER, sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scanId\":\"%s\"}".formatted(scanId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.turnsRemaining").value(3))
                .andExpect(jsonPath("$.data.messages[0].role").value("CHARACTER"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("conversationId").asText();
    }

    private Fixture fixture() {
        UUID sessionId = createSession();
        ScanEvent scan = scanEventRepository.save(new ScanEvent(
                sessionId, "SKY-STREAM-W260", "KR-SEONGSU", "ZONE04", Instant.now()));
        return new Fixture(sessionId, scan.getId());
    }

    private UUID createSession() {
        UUID sessionId = UUID.randomUUID();
        sessionRepository.save(new AnonymousSession(sessionId, Market.KR, AppLocale.KO, "TEST", Instant.now()));
        return sessionId;
    }

    private record Fixture(UUID sessionId, UUID scanId) {
    }
}
