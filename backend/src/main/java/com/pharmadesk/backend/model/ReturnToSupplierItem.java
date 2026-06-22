package com.pharmadesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "return_to_supplier_items")
@SQLDelete(sql = "UPDATE return_to_supplier_items SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class ReturnToSupplierItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    @JsonIgnore
    private ReturnToSupplier returnToSupplier;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "return_quantity")
    private Integer returnQuantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "line_total")
    private BigDecimal lineTotal;

    // Getters and Setters
    public ReturnToSupplier getReturnToSupplier() { return returnToSupplier; }
    public void setReturnToSupplier(ReturnToSupplier returnToSupplier) { this.returnToSupplier = returnToSupplier; }
    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public Integer getReturnQuantity() { return returnQuantity; }
    public void setReturnQuantity(Integer returnQuantity) { this.returnQuantity = returnQuantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
