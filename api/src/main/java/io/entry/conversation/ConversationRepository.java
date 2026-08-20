package io.entry.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findBySessionIdAndScanId(UUID sessionId, UUID scanId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select conversation from Conversation conversation where conversation.id = :id and conversation.sessionId = :sessionId")
    Optional<Conversation> findLockedByIdAndSessionId(@Param("id") UUID id, @Param("sessionId") UUID sessionId);
}
