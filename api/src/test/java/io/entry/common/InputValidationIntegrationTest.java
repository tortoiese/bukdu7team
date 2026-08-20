package io.entry.common;

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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InputValidationIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SessionRepository sessionRepository;

    UUID sessionId;

    @BeforeEach
    void 세션_준비() {
        sessionId = UUID.randomUUID();
        sessionRepository.save(new AnonymousSession(sessionId, Market.KR, AppLocale.KO, "TEST", Instant.now()));
    }

    @Test
    void 잘못된_market은_400이다() throws Exception {
        mvc.perform(get("/api/v1/archive?market=INVALID")
                        .header(SessionInterceptor.HEADER, sessionId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"));
    }

    @Test
    void 잘못된_JSON은_400이다() throws Exception {
        mvc.perform(post("/api/v1/archive")
                        .header(SessionInterceptor.HEADER, sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST_BODY"));
    }

    @Test
    void 잘못된_locale_enum은_400이다() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/sessions/market")
                        .header(SessionInterceptor.HEADER, sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"market\":\"US\",\"locale\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST_BODY"));
    }

    @Test
    void 잘못된_scanId는_400이다() throws Exception {
        mvc.perform(post("/api/v1/archive")
                        .header(SessionInterceptor.HEADER, sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"SKY-STREAM-W260","scanId":"not-a-uuid"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_SCAN_ID"));
    }
}
