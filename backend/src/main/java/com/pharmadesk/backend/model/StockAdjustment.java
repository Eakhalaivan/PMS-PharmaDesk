package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_adjustments")
@SQLDelete(sql = "UPDATE stock_adjustments SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class StockAdjustment extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_stock_id", nullable = false)
    private MedicineStock medicineStock;

    @Column(name = "adjusted_quantity", nullable = false)
    private Integer adjustedQuantity; // positive or negative

    @Column(nullable = false)
    private String reason; // Physical Count Correction, Damage, Theft, Sample

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjusted_by_user_id", nullable = false)
    private User adjustedBy;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    private String remarks;

    // Getters and Setters
    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public MedicineStock getMedicineStock() { return medicineStock; }
    public void setMedicineStock(MedicineStock medicineStock) { this.medicineStock = medicineStock; }

    public Integer getAdjustedQuantity() { return adjustedQuantity; }
    public void setAdjustedQuantity(Integer adjustedQuantity) { this.adjustedQuantity = adjustedQuantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public User getAdjustedBy() { return adjustedBy; }
    public void setAdjustedBy(User adjustedBy) { this.adjustedBy = adjustedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
