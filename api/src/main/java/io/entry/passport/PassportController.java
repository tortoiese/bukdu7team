package io.entry.passport;

import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.common.RequestContext;
import io.entry.passport.dto.PassportData;
import io.entry.passport.dto.StampResponse;
import io.entry.passport.dto.StampZoneRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/passport")
public class PassportController {

    private final PassportService passportService;

    public PassportController(PassportService passportService) {
        this.passportService = passportService;
    }

    @PostMapping
    public ApiResponse<PassportData> issue(@Valid @RequestBody PassportRequestDto request) {
        PassportData data = passportService.issue(sessionId(), request.popupId());
        return ApiResponse.of(data, ApiMeta.basic());
    }

    @GetMapping
    public ApiResponse<PassportData> get() {
        return ApiResponse.of(passportService.get(sessionId()), ApiMeta.basic());
    }

    @PostMapping("/stamps")
    public ApiResponse<StampResponse> stamp(@Valid @RequestBody StampZoneRequest request) {
        StampResponse response = passportService.stamp(sessionId(), request.zoneId());
        return ApiResponse.of(response, ApiMeta.basic());
    }

    private UUID sessionId() {
        return UUID.fromString(RequestContext.sessionId());
    }
}
