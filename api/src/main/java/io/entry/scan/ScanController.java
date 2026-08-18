package io.entry.scan;

import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.common.RequestContext;
import io.entry.scan.dto.ScanRequest;
import io.entry.scan.dto.ScanResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scans")
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping
    public ApiResponse<ScanResponse> scan(@Valid @RequestBody ScanRequest request) {
        UUID sessionId = UUID.fromString(RequestContext.sessionId());
        ScanResponse response = scanService.recordScan(sessionId, request);
        boolean aiUsed = response.intentSignal().aiUsed();
        return ApiResponse.of(response, ApiMeta.ai(aiUsed, !aiUsed));
    }
}
