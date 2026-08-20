package io.entry.recap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecapLinkRepository extends JpaRepository<RecapLink, UUID> {
    boolean existsBySessionId(UUID sessionId);

    /** 이메일로 돌아가기(/lookup) 전용 — 같은 이메일로 여러 번 연결했다면 가장 최근 세션을 돌려준다. */
    Optional<RecapLink> findTopByChannelAndValueHashOrderByLinkedAtDesc(String channel, String valueHash);
}
