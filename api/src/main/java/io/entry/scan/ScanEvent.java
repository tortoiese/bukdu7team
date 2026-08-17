package io.entry.scan;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scan_event")
public class ScanEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID sessionId;
    private String productId;
    private String storeId;
    private String zoneId;
    private Instant scannedAt;

    protected ScanEvent() {
    }

    public ScanEvent(UUID sessionId, String productId, String storeId, String zoneId, Instant scannedAt) {
        this.sessionId = sessionId;
        this.productId = productId;
        this.storeId = storeId;
        this.zoneId = zoneId;
        this.scannedAt = scannedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getProductId() {
        return productId;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getZoneId() {
        return zoneId;
    }

    public Instant getScannedAt() {
        return scannedAt;
    }
}
