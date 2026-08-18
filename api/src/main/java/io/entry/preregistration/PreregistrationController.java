package io.entry.preregistration;

import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.common.RequestContext;
import io.entry.preregistration.dto.PreregistrationRequest;
import io.entry.preregistration.dto.PreregistrationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/preregistrations")
public class PreregistrationController {

    private final PreregistrationService preregistrationService;

    public PreregistrationController(PreregistrationService preregistrationService) {
        this.preregistrationService = preregistrationService;
    }

    @PostMapping
    public ApiResponse<PreregistrationResponse> register(@Valid @RequestBody PreregistrationRequest request) {
        UUID sessionId = UUID.fromString(RequestContext.sessionId());
        PreregistrationResponse data = preregistrationService.register(sessionId, request);
        return ApiResponse.of(data, ApiMeta.basic());
    }
}
