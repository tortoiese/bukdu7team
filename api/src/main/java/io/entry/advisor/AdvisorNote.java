package io.entry.advisor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** 어드바이저가 남긴 상담 메모. 고객 세션 타임라인에 귀속되어 귀가 후에도 남는다. */
@Entity
@Table(name = "advisor_note")
public class AdvisorNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID sessionId;
    private String note;
    private Instant createdAt;

    protected AdvisorNote() {
    }

    public AdvisorNote(UUID sessionId, String note, Instant createdAt) {
        this.sessionId = sessionId;
        this.note = note;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }
}
