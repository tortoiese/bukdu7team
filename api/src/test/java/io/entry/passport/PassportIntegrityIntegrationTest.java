package io.entry.passport;

import io.entry.common.AppLocale;
import io.entry.common.Market;
import io.entry.session.AnonymousSession;
import io.entry.session.SessionInterceptor;
import io.entry.session.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Locale;
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
class PassportIntegrityIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired PassportService passportService;
    @Autowired PassportRepository passportRepository;
    @Autowired PassportStampRepository stampRepository;
    @Autowired SessionRepository sessionRepository;

    UUID sessionId;

    @BeforeEach
    void 데이터_준비() {
        stampRepository.deleteAll();
        passportRepository.deleteAll();
        sessionRepository.deleteAll();
        sessionId = UUID.randomUUID();
        sessionRepository.save(new AnonymousSession(sessionId, Market.KR, AppLocale.KO, "TEST", Instant.now()));
    }

    @Test
    void 존재하지_않는_존은_적립되지_않는다() throws Exception {
        passportService.issue(sessionId, "MCM-SEONGSU-2026");

        mvc.perform(post("/api/v1/passport/stamps")
                        .header(SessionInterceptor.HEADER, sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"zoneId\":\"ZONE99\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ZONE"));

        assertThat(stampRepository.count()).isZero();
    }

    @Test
    void 패스포트_번호는_세션_UUID에서_결정되고_반복_발급에도_같다() {
        String expected = "ENT-KR-" + sessionId.toString().replace("-", "").toUpperCase(Locale.ROOT);

        var first = passportService.issue(sessionId, "MCM-SEONGSU-2026");
        var second = passportService.issue(sessionId, "MCM-SEONGSU-2026");

        assertThat(first.passportNo()).isEqualTo(expected);
        assertThat(second.passportNo()).isEqualTo(first.passportNo());
        assertThat(passportRepository.count()).isEqualTo(1);
    }

    @Test
    void 동시에_발급해도_같은_패스포트_하나만_반환한다() throws Exception {
        int requestCount = 6;
        CyclicBarrier barrier = new CyclicBarrier(requestCount);
        var executor = Executors.newFixedThreadPool(requestCount);

        try {
            var futures = java.util.stream.IntStream.range(0, requestCount)
                    .mapToObj(ignored -> executor.submit(() -> {
                        barrier.await();
                        return passportService.issue(sessionId, "MCM-SEONGSU-2026").passportNo();
                    }))
                    .toList();
            Set<String> numbers = new java.util.HashSet<>();
            for (var future : futures) {
                numbers.add(future.get(10, TimeUnit.SECONDS));
            }

            assertThat(numbers).hasSize(1);
            assertThat(passportRepository.count()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
