package io.entry.scan.dto;

import io.entry.intent.IntentSignal;

public record ScanResponse(
        String scanId,
        long scanCountForProduct,
        long sessionScanCount,
        IntentSignal intentSignal,
        GreetingData greeting
) {
}
