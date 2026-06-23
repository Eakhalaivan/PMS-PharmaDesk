package com.pharmadesk.backend.pharmacy.dto.dashboard;

import java.time.LocalDateTime;

public class SystemAlertDTO {
    private String id;
    private String title;
    private String description;
    private String severity; // INFO, WARNING, CRITICAL
    private String category; // STOCK, COLD_CHAIN, EXPIRY, GRN, PRESCRIPTION, SYSTEM
    private LocalDateTime createdAt;
    private boolean resolved;

    public SystemAlertDTO() {}

    public SystemAlertDTO(String id, String title, String description, String severity, String category, LocalDateTime createdAt, boolean resolved) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.category = category;
        this.createdAt = createdAt;
        this.resolved = resolved;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
}
