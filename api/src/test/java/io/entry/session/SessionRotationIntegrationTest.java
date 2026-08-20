package io.entry.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.entry.scan.ScanEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionRotationIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ScanEventRepository scanEventRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void 무효_세션으로_스캔하면_응답_헤더의_새_세션에_기록된다() throws Exception {
        var result = mvc.perform(post("/api/v1/scans")
                        .header(SessionInterceptor.HEADER, "invalid-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": "SKY-STREAM-W260",
                                  "storeId": "KR-SEONGSU",
                                  "zoneId": "ZONE04"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.sessionRotated").value(true))
                .andReturn();

        String rotated = result.getResponse().getHeader(SessionInterceptor.HEADER);
        assertThat(rotated).isNotBlank();
        assertThat(scanEventRepository.findBySessionIdOrderByScannedAtAsc(UUID.fromString(rotated))).hasSize(1);
    }

    @Test
    void 무효_세션을_현재_세션_조회로_먼저_확정할_수_있다() throws Exception {
        var result = mvc.perform(get("/api/v1/sessions/current")
                        .header(SessionInterceptor.HEADER, "invalid-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.sessionRotated").value(true))
                .andReturn();

        String sessionId = result.getResponse().getHeader(SessionInterceptor.HEADER);
        String bodySessionId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("sessionId").asText();
        assertThat(bodySessionId).isEqualTo(sessionId);
    }
}
