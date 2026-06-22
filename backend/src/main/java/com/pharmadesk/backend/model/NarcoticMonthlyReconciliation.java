package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "narcotic_monthly_reconciliation")
public class NarcoticMonthlyReconciliation {

    @Id
    @Column(name = "reconciliation_id", length = 36)
    private String reconciliationId = java.util.UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(name = "reconciliation_month", nullable = false)
    private Integer reconciliationMonth;

    @Column(name = "reconciliation_year", nullable = false)
    private Integer reconciliationYear;

    @Column(name = "opening_stock", nullable = false)
    private Integer openingStock;

    @Column(name = "total_received")
    private Integer totalReceived = 0;

    @Column(name = "total_dispensed", nullable = false)
    private Integer totalDispensed = 0;

    @Column(name = "total_returned")
    private Integer totalReturned = 0;

    @Column(name = "total_written_off")
    private Integer totalWrittenOff = 0;

    @Column(name = "closing_stock_calculated", nullable = false)
    private Integer closingStockCalculated;

    @Column(name = "closing_stock_physical")
    private Integer closingStockPhysical;

    @Column(name = "variance", insertable = false, updatable = false)
    private Integer variance;

    @Column(name = "is_balanced")
    private boolean balanced = false;

    @Column(name = "reconciled_by")
    private Long reconciledBy;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt = LocalDateTime.now();

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(length = 1000)
    private String remarks;

    // Getters and Setters
    public String getReconciliationId() { return reconciliationId; }
    public void setReconciliationId(String reconciliationId) { this.reconciliationId = reconciliationId; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Integer getReconciliationMonth() { return reconciliationMonth; }
    public void setReconciliationMonth(Integer reconciliationMonth) { this.reconciliationMonth = reconciliationMonth; }

    public Integer getReconciliationYear() { return reconciliationYear; }
    public void setReconciliationYear(Integer reconciliationYear) { this.reconciliationYear = reconciliationYear; }

    public Integer getOpeningStock() { return openingStock; }
    public void setOpeningStock(Integer openingStock) { this.openingStock = openingStock; }

    public Integer getTotalReceived() { return totalReceived; }
    public void setTotalReceived(Integer totalReceived) { this.totalReceived = totalReceived; }

    public Integer getTotalDispensed() { return totalDispensed; }
    public void setTotalDispensed(Integer totalDispensed) { this.totalDispensed = totalDispensed; }

    public Integer getClosingStockCalculated() { return closingStockCalculated; }
    public void setClosingStockCalculated(Integer closingStockCalculated) { this.closingStockCalculated = closingStockCalculated; }

    public Integer getClosingStockPhysical() { return closingStockPhysical; }
    public void setClosingStockPhysical(Integer closingStockPhysical) { this.closingStockPhysical = closingStockPhysical; }

    public Integer getVariance() { return variance; }

    public boolean isBalanced() { return balanced; }
    public void setBalanced(boolean balanced) { this.balanced = balanced; }

    public Long getReconciledBy() { return reconciledBy; }
    public void setReconciledBy(Long reconciledBy) { this.reconciledBy = reconciledBy; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
