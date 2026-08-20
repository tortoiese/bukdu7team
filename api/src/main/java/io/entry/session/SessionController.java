package io.entry.session;

import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.common.RequestContext;
import io.entry.session.dto.CreateSessionRequest;
import io.entry.session.dto.SessionData;
import io.entry.session.dto.UpdateMarketRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ApiResponse<SessionData> create(@Valid @RequestBody CreateSessionRequest request) {
        return ApiResponse.of(sessionService.create(request), ApiMeta.basic());
    }

    @PatchMapping("/market")
    public ApiResponse<SessionData> updateMarket(@Valid @RequestBody UpdateMarketRequest request) {
        SessionData data = sessionService.updateMarket(RequestContext.sessionId(), request);
        return ApiResponse.of(data, ApiMeta.basic());
    }

    @GetMapping("/current")
    public ApiResponse<SessionData> current() {
        return ApiResponse.of(sessionService.current(RequestContext.sessionId()), ApiMeta.basic());
    }
}
