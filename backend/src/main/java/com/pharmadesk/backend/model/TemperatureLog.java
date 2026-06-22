package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "temperature_logs")
public class TemperatureLog {

    @Id
    @Column(name = "log_id", length = 36)
    private String logId = java.util.UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id", nullable = false)
    private StorageUnit storageUnit;

    @Column(name = "unit_name", nullable = false, length = 100)
    private String unitName;

    @Column(name = "unit_type", length = 30)
    private String unitType;

    @Column(name = "recorded_temperature", nullable = false)
    private BigDecimal recordedTemperature;

    @Column(name = "min_threshold", nullable = false)
    private BigDecimal minThreshold;

    @Column(name = "max_threshold", nullable = false)
    private BigDecimal maxThreshold;

    @Column(name = "log_type", length = 30)
    private String logType = "manual";

    @Column(name = "is_breach", insertable = false, updatable = false)
    private boolean breach;

    @Column(name = "breach_severity", length = 20)
    private String breachSeverity;

    @Column(name = "breach_notes", columnDefinition = "TEXT")
    private String breachNotes;

    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    @Column(name = "corrective_action_by")
    private Long correctiveActionBy;

    @Column(name = "corrective_action_at")
    private LocalDateTime correctiveActionAt;

    @Column(name = "medicines_affected", columnDefinition = "TEXT")
    private String medicinesAffected;

    @Column(name = "recorded_by", nullable = false)
    private Long recordedBy;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt = LocalDateTime.now();

    // Getters and Setters
    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public StorageUnit getStorageUnit() { return storageUnit; }
    public void setStorageUnit(StorageUnit storageUnit) { this.storageUnit = storageUnit; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public BigDecimal getRecordedTemperature() { return recordedTemperature; }
    public void setRecordedTemperature(BigDecimal recordedTemperature) { this.recordedTemperature = recordedTemperature; }

    public BigDecimal getMinThreshold() { return minThreshold; }
    public void setMinThreshold(BigDecimal minThreshold) { this.minThreshold = minThreshold; }

    public BigDecimal getMaxThreshold() { return maxThreshold; }
    public void setMaxThreshold(BigDecimal maxThreshold) { this.maxThreshold = maxThreshold; }

    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }

    public boolean isBreach() { return breach; }

    public String getBreachSeverity() { return breachSeverity; }
    public void setBreachSeverity(String breachSeverity) { this.breachSeverity = breachSeverity; }

    public String getBreachNotes() { return breachNotes; }
    public void setBreachNotes(String breachNotes) { this.breachNotes = breachNotes; }

    public String getCorrectiveAction() { return correctiveAction; }
    public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }

    public Long getCorrectiveActionBy() { return correctiveActionBy; }
    public void setCorrectiveActionBy(Long correctiveActionBy) { this.correctiveActionBy = correctiveActionBy; }

    public LocalDateTime getCorrectiveActionAt() { return correctiveActionAt; }
    public void setCorrectiveActionAt(LocalDateTime correctiveActionAt) { this.correctiveActionAt = correctiveActionAt; }

    public String getMedicinesAffected() { return medicinesAffected; }
    public void setMedicinesAffected(String medicinesAffected) { this.medicinesAffected = medicinesAffected; }

    public Long getRecordedBy() { return recordedBy; }
    public void setRecordedBy(Long recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
