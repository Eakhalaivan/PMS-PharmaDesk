package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_medicine_coverage")
public class InsuranceMedicineCoverage {

    @Id
    @Column(name = "coverage_id", length = 36)
    private String coverageId = java.util.UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id", nullable = false)
    private InsuranceProvider provider;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(name = "is_covered")
    private boolean covered = true;

    @Column(name = "coverage_percentage")
    private BigDecimal coveragePercentage = BigDecimal.valueOf(100.00);

    @Column(name = "max_coverage_amount")
    private BigDecimal maxCoverageAmount;

    @Column(name = "coverage_notes", columnDefinition = "TEXT")
    private String coverageNotes;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
    public String getCoverageId() { return coverageId; }
    public void setCoverageId(String coverageId) { this.coverageId = coverageId; }

    public InsuranceProvider getProvider() { return provider; }
    public void setProvider(InsuranceProvider provider) { this.provider = provider; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public boolean isCovered() { return covered; }
    public void setCovered(boolean covered) { this.covered = covered; }

    public BigDecimal getCoveragePercentage() { return coveragePercentage; }
    public void setCoveragePercentage(BigDecimal coveragePercentage) { this.coveragePercentage = coveragePercentage; }

    public BigDecimal getMaxCoverageAmount() { return maxCoverageAmount; }
    public void setMaxCoverageAmount(BigDecimal maxCoverageAmount) { this.maxCoverageAmount = maxCoverageAmount; }

    public String getCoverageNotes() { return coverageNotes; }
    public void setCoverageNotes(String coverageNotes) { this.coverageNotes = coverageNotes; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
}
