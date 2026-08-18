package io.entry.advisor;

import io.entry.advisor.dto.AdvisorBriefingData;
import io.entry.advisor.dto.ConsentRequest;
import io.entry.advisor.dto.ConsentResponse;
import io.entry.advisor.dto.NoteRequest;
import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.common.RequestContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class AdvisorController {

    private final AdvisorService advisorService;

    public AdvisorController(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @PostMapping("/api/v1/consents/advisor")
    public ApiResponse<ConsentResponse> issueConsent(@Valid @RequestBody ConsentRequest request) {
        UUID sessionId = UUID.fromString(RequestContext.sessionId());
        return ApiResponse.of(advisorService.issueConsent(sessionId, request), ApiMeta.basic());
    }

    @GetMapping("/api/v1/advisor/{grantToken}")
    public ApiResponse<AdvisorBriefingData> getBriefing(@PathVariable String grantToken) {
        AdvisorBriefingData data = advisorService.getBriefing(grantToken);
        return ApiResponse.of(data, ApiMeta.ai(data.briefing().aiUsed(), !data.briefing().aiUsed()));
    }

    @PostMapping("/api/v1/advisor/{grantToken}/notes")
    public ApiResponse<Map<String, Boolean>> addNote(@PathVariable String grantToken, @Valid @RequestBody NoteRequest request) {
        advisorService.addNote(grantToken, request.note());
        return ApiResponse.of(Map.of("saved", true), ApiMeta.basic());
    }
}
