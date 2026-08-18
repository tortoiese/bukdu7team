package io.entry.preregistration;

import io.entry.preregistration.dto.PreregistrationRequest;
import io.entry.preregistration.dto.PreregistrationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/** P8 사전 등록. 제공 혜택은 전용 시간대 우선 입장뿐이다(CLAUDE.md R2, F4 보상 설계). */
@Service
public class PreregistrationService {

    private static final String[] TIME_WINDOWS = {"11:00–12:00", "14:00–15:00", "16:00–17:00"};

    private final PreregistrationRepository repository;

    public PreregistrationService(PreregistrationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PreregistrationResponse register(UUID sessionId, PreregistrationRequest request) {
        long count = repository.count();
        String code = String.format("PRE-%04d", count + 1);
        String timeWindow = TIME_WINDOWS[(int) (count % TIME_WINDOWS.length)];

        Preregistration entity = new Preregistration(
                sessionId, request.channel(), sha256(request.value()), request.interestedLines(),
                request.market(), request.consent(), code, timeWindow, Instant.now());
        repository.save(entity);

        return new PreregistrationResponse("PRIORITY_ENTRY", timeWindow, code);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
