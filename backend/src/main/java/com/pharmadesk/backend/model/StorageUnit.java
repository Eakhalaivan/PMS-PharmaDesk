package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "storage_units")
public class StorageUnit {

    @Id
    @Column(name = "unit_id", length = 36)
    private String unitId = java.util.UUID.randomUUID().toString();

    @Column(name = "unit_name", nullable = false, length = 100)
    private String unitName;

    @Column(name = "unit_type", nullable = false, length = 30)
    private String unitType;

    @Column(length = 100)
    private String location;

    @Column(name = "min_threshold", nullable = false)
    private BigDecimal minThreshold;

    @Column(name = "max_threshold", nullable = false)
    private BigDecimal maxThreshold;

    @Column(name = "is_iot_enabled")
    private boolean iotEnabled = false;

    @Column(name = "iot_device_id", length = 60)
    private String iotDeviceId;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "assigned_medicines", columnDefinition = "TEXT")
    private String assignedMedicines;

    // Getters and Setters
    public String getUnitId() { return unitId; }
    public void setUnitId(String unitId) { this.unitId = unitId; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getMinThreshold() { return minThreshold; }
    public void setMinThreshold(BigDecimal minThreshold) { this.minThreshold = minThreshold; }

    public BigDecimal getMaxThreshold() { return maxThreshold; }
    public void setMaxThreshold(BigDecimal maxThreshold) { this.maxThreshold = maxThreshold; }

    public boolean isIotEnabled() { return iotEnabled; }
    public void setIotEnabled(boolean iotEnabled) { this.iotEnabled = iotEnabled; }

    public String getIotDeviceId() { return iotDeviceId; }
    public void setIotDeviceId(String iotDeviceId) { this.iotDeviceId = iotDeviceId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getAssignedMedicines() { return assignedMedicines; }
    public void setAssignedMedicines(String assignedMedicines) { this.assignedMedicines = assignedMedicines; }
}
