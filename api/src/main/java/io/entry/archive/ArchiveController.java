package io.entry.archive;

import io.entry.archive.dto.ArchiveListData;
import io.entry.archive.dto.ArchiveSaveRequest;
import io.entry.archive.dto.SavedCountResponse;
import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.common.Market;
import io.entry.common.RequestContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/archive")
public class ArchiveController {

    private final ArchiveService archiveService;

    public ArchiveController(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @PostMapping
    public ApiResponse<SavedCountResponse> save(@Valid @RequestBody ArchiveSaveRequest request) {
        long count = archiveService.save(sessionId(), request);
        return ApiResponse.of(new SavedCountResponse(count), ApiMeta.basic());
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<SavedCountResponse> delete(@PathVariable String productId) {
        long count = archiveService.delete(sessionId(), productId);
        return ApiResponse.of(new SavedCountResponse(count), ApiMeta.basic());
    }

    @GetMapping
    public ApiResponse<ArchiveListData> list(@RequestParam Market market) {
        ArchiveListData data = archiveService.list(sessionId(), market);
        return ApiResponse.of(data, ApiMeta.ai(data.intentSummary().aiUsed(), !data.intentSummary().aiUsed()));
    }

    private UUID sessionId() {
        return UUID.fromString(RequestContext.sessionId());
    }
}
