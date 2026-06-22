package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "supplier_performance")
@SQLDelete(sql = "UPDATE supplier_performance SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class SupplierPerformance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "on_time_delivery_rate")
    private Double onTimeDeliveryRate;

    @Column(name = "order_fill_rate")
    private Double orderFillRate;

    @Column(name = "quality_rejection_rate")
    private Double qualityRejectionRate;

    @Column(name = "price_variance_rate")
    private Double priceVarianceRate;

    @Column(name = "invoice_accuracy_rate")
    private Double invoiceAccuracyRate;

    @Column(name = "return_rate")
    private Double returnRate;

    @Column(name = "credit_note_response_days")
    private Double creditNoteResponseDays;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "manual_notes", length = 1000)
    private String manualNotes;

    // Getters and Setters
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public Double getOnTimeDeliveryRate() { return onTimeDeliveryRate; }
    public void setOnTimeDeliveryRate(Double onTimeDeliveryRate) { this.onTimeDeliveryRate = onTimeDeliveryRate; }
    public Double getOrderFillRate() { return orderFillRate; }
    public void setOrderFillRate(Double orderFillRate) { this.orderFillRate = orderFillRate; }
    public Double getQualityRejectionRate() { return qualityRejectionRate; }
    public void setQualityRejectionRate(Double qualityRejectionRate) { this.qualityRejectionRate = qualityRejectionRate; }
    public Double getPriceVarianceRate() { return priceVarianceRate; }
    public void setPriceVarianceRate(Double priceVarianceRate) { this.priceVarianceRate = priceVarianceRate; }
    public Double getInvoiceAccuracyRate() { return invoiceAccuracyRate; }
    public void setInvoiceAccuracyRate(Double invoiceAccuracyRate) { this.invoiceAccuracyRate = invoiceAccuracyRate; }
    public Double getReturnRate() { return returnRate; }
    public void setReturnRate(Double returnRate) { this.returnRate = returnRate; }
    public Double getCreditNoteResponseDays() { return creditNoteResponseDays; }
    public void setCreditNoteResponseDays(Double creditNoteResponseDays) { this.creditNoteResponseDays = creditNoteResponseDays; }
    public Double getOverallScore() { return overallScore; }
    public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }
    public String getManualNotes() { return manualNotes; }
    public void setManualNotes(String manualNotes) { this.manualNotes = manualNotes; }
}
