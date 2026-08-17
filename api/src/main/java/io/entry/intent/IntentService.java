package io.entry.intent;

import io.entry.scan.ScanEvent;
import io.entry.scan.ScanEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** 세션의 현재 의도 신호를 여러 도메인(패스포트 MRZ, 리캡, 국경 이전, 어드바이저)에서 공유해 쓴다. */
@Service
public class IntentService {

    private final ScanEventRepository scanEventRepository;
    private final RuleIntentAnalyzer analyzer;

    public IntentService(ScanEventRepository scanEventRepository, RuleIntentAnalyzer analyzer) {
        this.scanEventRepository = scanEventRepository;
        this.analyzer = analyzer;
    }

    public Optional<IntentSignal> currentSignal(UUID sessionId) {
        List<ScanEvent> history = scanEventRepository.findBySessionIdOrderByScannedAtAsc(sessionId);
        if (history.isEmpty()) return Optional.empty();
        String lastProductId = history.get(history.size() - 1).getProductId();
        List<String> productIds = history.stream().map(ScanEvent::getProductId).toList();
        return Optional.of(analyzer.analyze(productIds, lastProductId));
    }

    public Optional<ScanEvent> lastScan(UUID sessionId) {
        List<ScanEvent> history = scanEventRepository.findBySessionIdOrderByScannedAtAsc(sessionId);
        if (history.isEmpty()) return Optional.empty();
        return Optional.of(history.get(history.size() - 1));
    }
}
