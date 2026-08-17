package io.entry.archive;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saved_item", uniqueConstraints = @UniqueConstraint(columnNames = {"sessionId", "productId"}))
public class SavedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID sessionId;
    private String productId;
    private String savedAtStoreId;
    private String zoneId;
    private Instant savedAt;

    protected SavedItem() {
    }

    public SavedItem(UUID sessionId, String productId, String savedAtStoreId, String zoneId, Instant savedAt) {
        this.sessionId = sessionId;
        this.productId = productId;
        this.savedAtStoreId = savedAtStoreId;
        this.zoneId = zoneId;
        this.savedAt = savedAt;
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

    public String getSavedAtStoreId() {
        return savedAtStoreId;
    }

    public String getZoneId() {
        return zoneId;
    }

    public Instant getSavedAt() {
        return savedAt;
    }
}
