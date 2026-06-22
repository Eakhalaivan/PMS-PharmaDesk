package com.pharmadesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "goods_receipt_note_items")
@SQLDelete(sql = "UPDATE goods_receipt_note_items SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class GoodsReceiptNoteItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    @JsonIgnore
    private GoodsReceiptNote goodsReceiptNote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "po_item_id")
    private Long poItemId;

    @Column(name = "ordered_quantity")
    private Integer orderedQuantity;

    @Column(name = "received_quantity")
    private Integer receivedQuantity;

    @Column(name = "rejected_quantity")
    private Integer rejectedQuantity = 0;

    @Column(name = "rejection_reason")
    private String rejectionReason; // Damaged, Wrong Item, Short Expiry, Quality Fail

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "mrp")
    private BigDecimal mrp;

    @Column(name = "purchase_rate")
    private BigDecimal purchaseRate;

    // Getters and Setters
    public GoodsReceiptNote getGoodsReceiptNote() { return goodsReceiptNote; }
    public void setGoodsReceiptNote(GoodsReceiptNote goodsReceiptNote) { this.goodsReceiptNote = goodsReceiptNote; }
    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
    public Long getPoItemId() { return poItemId; }
    public void setPoItemId(Long poItemId) { this.poItemId = poItemId; }
    public Integer getOrderedQuantity() { return orderedQuantity; }
    public void setOrderedQuantity(Integer orderedQuantity) { this.orderedQuantity = orderedQuantity; }
    public Integer getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public Integer getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(Integer rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDate manufacturingDate) { this.manufacturingDate = manufacturingDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }
    public BigDecimal getPurchaseRate() { return purchaseRate; }
    public void setPurchaseRate(BigDecimal purchaseRate) { this.purchaseRate = purchaseRate; }
}
