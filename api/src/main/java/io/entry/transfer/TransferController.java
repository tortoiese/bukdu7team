package io.entry.transfer;

import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.common.Market;
import io.entry.common.RequestContext;
import io.entry.transfer.dto.TransferData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping("/api/v1/transfer")
    public ApiResponse<TransferData> get(@RequestParam Market market) {
        TransferData data = transferService.get(sessionId(), market);
        boolean aiUsed = data.sendTiming().aiUsed();
        return ApiResponse.of(data, ApiMeta.ai(aiUsed, !aiUsed));
    }

    private UUID sessionId() {
        return UUID.fromString(RequestContext.sessionId());
    }
}
