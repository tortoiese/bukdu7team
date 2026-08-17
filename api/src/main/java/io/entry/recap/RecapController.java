package io.entry.recap;

import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.common.RequestContext;
import io.entry.recap.dto.LinkAccountRequest;
import io.entry.recap.dto.LinkAccountResponse;
import io.entry.recap.dto.RecapData;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recap")
public class RecapController {

    private final RecapService recapService;

    public RecapController(RecapService recapService) {
        this.recapService = recapService;
    }

    @GetMapping
    public ApiResponse<RecapData> get() {
        RecapData data = recapService.get(sessionId());
        return ApiResponse.of(data, ApiMeta.ai(data.interestSummary().aiUsed(), !data.interestSummary().aiUsed()));
    }

    @PostMapping("/link")
    public ApiResponse<LinkAccountResponse> link(@Valid @RequestBody LinkAccountRequest request) {
        boolean linked = recapService.link(sessionId(), request);
        return ApiResponse.of(new LinkAccountResponse(linked), ApiMeta.basic());
    }

    private UUID sessionId() {
        return UUID.fromString(RequestContext.sessionId());
    }
}
