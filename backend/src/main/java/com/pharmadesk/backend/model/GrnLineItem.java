package com.pharmadesk.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "grn_line_items")
public class GrnLineItem {

    @Id
    @Column(name = "grn_line_id", length = 36)
    private String grnLineId = java.util.UUID.randomUUID().toString();

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    private GrnEntry goodsReceiptNote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(name = "batch_number", nullable = false, length = 60)
    private String batchNumber;

    @Column(name = "manufacturing_date", nullable = false)
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "po_quantity")
    private Integer poQuantity;

    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity;

    @Column(name = "rejected_quantity")
    private Integer rejectedQuantity = 0;

    @Column(name = "accepted_quantity", insertable = false, updatable = false)
    private Integer acceptedQuantity;

    @Column(name = "rejection_reason", length = 50)
    private String rejectionReason;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "mrp", nullable = false)
    private BigDecimal mrp;

    @Column(name = "gst_percentage", nullable = false)
    private BigDecimal gstPercentage;

    @Column(name = "hsn_code", length = 10)
    private String hsnCode;

    @Column(name = "line_subtotal", nullable = false)
    private BigDecimal lineSubtotal;

    @Column(name = "line_gst", nullable = false)
    private BigDecimal lineGst;

    @Column(name = "line_total", nullable = false)
    private BigDecimal lineTotal;

    @Column(name = "quantity_variance", insertable = false, updatable = false)
    private Integer quantityVariance;

    @Column(name = "price_variance")
    private BigDecimal priceVariance;

    @Column(name = "has_discrepancy")
    private boolean hasDiscrepancy = false;

    @Column(name = "discrepancy_type", length = 100)
    private String discrepancyType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id")
    private StockBatch stockBatch;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
    public String getGrnLineId() { return grnLineId; }
    public void setGrnLineId(String grnLineId) { this.grnLineId = grnLineId; }

    public GrnEntry getGoodsReceiptNote() { return goodsReceiptNote; }
    public void setGoodsReceiptNote(GrnEntry goodsReceiptNote) { this.goodsReceiptNote = goodsReceiptNote; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDate manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Integer getPoQuantity() { return poQuantity; }
    public void setPoQuantity(Integer poQuantity) { this.poQuantity = poQuantity; }

    public Integer getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; }

    public Integer getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(Integer rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }

    public Integer getAcceptedQuantity() { return acceptedQuantity; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }

    public BigDecimal getGstPercentage() { return gstPercentage; }
    public void setGstPercentage(BigDecimal gstPercentage) { this.gstPercentage = gstPercentage; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public BigDecimal getLineSubtotal() { return lineSubtotal; }
    public void setLineSubtotal(BigDecimal lineSubtotal) { this.lineSubtotal = lineSubtotal; }

    public BigDecimal getLineGst() { return lineGst; }
    public void setLineGst(BigDecimal lineGst) { this.lineGst = lineGst; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public Integer getQuantityVariance() { return quantityVariance; }

    public BigDecimal getPriceVariance() { return priceVariance; }
    public void setPriceVariance(BigDecimal priceVariance) { this.priceVariance = priceVariance; }

    public boolean isHasDiscrepancy() { return hasDiscrepancy; }
    public void setHasDiscrepancy(boolean hasDiscrepancy) { this.hasDiscrepancy = hasDiscrepancy; }

    public String getDiscrepancyType() { return discrepancyType; }
    public void setDiscrepancyType(String discrepancyType) { this.discrepancyType = discrepancyType; }

    public StockBatch getStockBatch() { return stockBatch; }
    public void setStockBatch(StockBatch stockBatch) { this.stockBatch = stockBatch; }
}
