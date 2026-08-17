package io.entry.passport;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "passport")
public class Passport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID sessionId;
    private String passportNo;
    private String popupId;
    private String issuedPlace;
    private Instant issuedAt;

    protected Passport() {
    }

    public Passport(UUID sessionId, String passportNo, String popupId, String issuedPlace, Instant issuedAt) {
        this.sessionId = sessionId;
        this.passportNo = passportNo;
        this.popupId = popupId;
        this.issuedPlace = issuedPlace;
        this.issuedAt = issuedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getPassportNo() {
        return passportNo;
    }

    public String getPopupId() {
        return popupId;
    }

    public String getIssuedPlace() {
        return issuedPlace;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}
