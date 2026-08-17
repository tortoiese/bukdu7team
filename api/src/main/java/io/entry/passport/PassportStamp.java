package io.entry.passport;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "passport_stamp", uniqueConstraints = @UniqueConstraint(columnNames = {"passportId", "zoneId"}))
public class PassportStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID passportId;
    private String zoneId;
    private Instant stampedAt;
    private int rotationSeed;

    protected PassportStamp() {
    }

    public PassportStamp(UUID passportId, String zoneId, Instant stampedAt, int rotationSeed) {
        this.passportId = passportId;
        this.zoneId = zoneId;
        this.stampedAt = stampedAt;
        this.rotationSeed = rotationSeed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPassportId() {
        return passportId;
    }

    public String getZoneId() {
        return zoneId;
    }

    public Instant getStampedAt() {
        return stampedAt;
    }

    public int getRotationSeed() {
        return rotationSeed;
    }
}
