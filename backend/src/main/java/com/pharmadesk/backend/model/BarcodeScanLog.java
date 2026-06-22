package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "barcode_scan_logs")
public class BarcodeScanLog {

    @Id
    @Column(name = "scan_id", length = 36)
    private String scanId = java.util.UUID.randomUUID().toString();

    @Column(name = "barcode_value", nullable = false, length = 200)
    private String barcodeValue;

    @Column(name = "scan_type", nullable = false, length = 30)
    private String scanType;

    @Column(name = "resolved_medicine_id")
    private Long resolvedMedicineId;

    @Column(name = "resolved_batch_id")
    private String resolvedBatchId;

    @Column(name = "scanned_by", nullable = false)
    private Long scannedBy;

    @Column(name = "scan_module", length = 60)
    private String scanModule;

    @Column(name = "scan_result", nullable = false, length = 30)
    private String scanResult;

    @Column(name = "scanned_at")
    private LocalDateTime scannedAt = LocalDateTime.now();

    // Getters and Setters
    public String getScanId() { return scanId; }
    public void setScanId(String scanId) { this.scanId = scanId; }

    public String getBarcodeValue() { return barcodeValue; }
    public void setBarcodeValue(String barcodeValue) { this.barcodeValue = barcodeValue; }

    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }

    public Long getResolvedMedicineId() { return resolvedMedicineId; }
    public void setResolvedMedicineId(Long resolvedMedicineId) { this.resolvedMedicineId = resolvedMedicineId; }

    public String getResolvedBatchId() { return resolvedBatchId; }
    public void setResolvedBatchId(String resolvedBatchId) { this.resolvedBatchId = resolvedBatchId; }

    public Long getScannedBy() { return scannedBy; }
    public void setScannedBy(Long scannedBy) { this.scannedBy = scannedBy; }

    public String getScanModule() { return scanModule; }
    public void setScanModule(String scanModule) { this.scanModule = scanModule; }

    public String getScanResult() { return scanResult; }
    public void setScanResult(String scanResult) { this.scanResult = scanResult; }

    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }
}
