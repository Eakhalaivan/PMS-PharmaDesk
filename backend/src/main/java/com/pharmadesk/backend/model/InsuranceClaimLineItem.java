package com.pharmadesk.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_claim_line_items")
public class InsuranceClaimLineItem {

    @Id
    @Column(name = "claim_line_id", length = 36)
    private String claimLineId = java.util.UUID.randomUUID().toString();

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private InsuranceClaim insuranceClaim;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(name = "bill_line_item_id")
    private Long billLineItemId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false)
    private BigDecimal lineTotal;

    @Column(name = "is_covered", nullable = false)
    private boolean covered = true;

    @Column(name = "coverage_percentage")
    private BigDecimal coveragePercentage;

    @Column(name = "covered_amount")
    private BigDecimal coveredAmount = BigDecimal.ZERO;

    @Column(name = "non_covered_amount")
    private BigDecimal nonCoveredAmount = BigDecimal.ZERO;

    @Column(name = "non_covered_reason", length = 150)
    private String nonCoveredReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
    public String getClaimLineId() { return claimLineId; }
    public void setClaimLineId(String claimLineId) { this.claimLineId = claimLineId; }

    public InsuranceClaim getInsuranceClaim() { return insuranceClaim; }
    public void setInsuranceClaim(InsuranceClaim insuranceClaim) { this.insuranceClaim = insuranceClaim; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Long getBillLineItemId() { return billLineItemId; }
    public void setBillLineItemId(Long billLineItemId) { this.billLineItemId = billLineItemId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public boolean isCovered() { return covered; }
    public void setCovered(boolean covered) { this.covered = covered; }

    public BigDecimal getCoveragePercentage() { return coveragePercentage; }
    public void setCoveragePercentage(BigDecimal coveragePercentage) { this.coveragePercentage = coveragePercentage; }

    public BigDecimal getCoveredAmount() { return coveredAmount; }
    public void setCoveredAmount(BigDecimal coveredAmount) { this.coveredAmount = coveredAmount; }

    public BigDecimal getNonCoveredAmount() { return nonCoveredAmount; }
    public void setNonCoveredAmount(BigDecimal nonCoveredAmount) { this.nonCoveredAmount = nonCoveredAmount; }

    public String getNonCoveredReason() { return nonCoveredReason; }
    public void setNonCoveredReason(String nonCoveredReason) { this.nonCoveredReason = nonCoveredReason; }
}
