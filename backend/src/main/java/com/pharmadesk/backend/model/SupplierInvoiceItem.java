package com.pharmadesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_invoice_items")
@SQLDelete(sql = "UPDATE supplier_invoice_items SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class SupplierInvoiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    private SupplierInvoice supplierInvoice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "po_item_id")
    private Long poItemId;

    @Column(name = "grn_item_id")
    private Long grnItemId;

    @Column(name = "billed_quantity")
    private Integer billedQuantity;

    @Column(name = "billed_price")
    private BigDecimal billedPrice;

    @Column(name = "gst_percentage")
    private Double gstPercentage;

    @Column(name = "line_total")
    private BigDecimal lineTotal;

    // NONE, PRICE_VARIANCE, QTY_OVERBILLED, GST_MISMATCH, GSTIN_MISMATCH
    @Column(name = "discrepancy_type")
    private String discrepancyType;

    // BLOCK, WARNING, ADVISORY
    @Column(name = "discrepancy_severity")
    private String discrepancySeverity;

    // Getters and Setters
    public SupplierInvoice getSupplierInvoice() { return supplierInvoice; }
    public void setSupplierInvoice(SupplierInvoice supplierInvoice) { this.supplierInvoice = supplierInvoice; }
    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
    public Long getPoItemId() { return poItemId; }
    public void setPoItemId(Long poItemId) { this.poItemId = poItemId; }
    public Long getGrnItemId() { return grnItemId; }
    public void setGrnItemId(Long grnItemId) { this.grnItemId = grnItemId; }
    public Integer getBilledQuantity() { return billedQuantity; }
    public void setBilledQuantity(Integer billedQuantity) { this.billedQuantity = billedQuantity; }
    public BigDecimal getBilledPrice() { return billedPrice; }
    public void setBilledPrice(BigDecimal billedPrice) { this.billedPrice = billedPrice; }
    public Double getGstPercentage() { return gstPercentage; }
    public void setGstPercentage(Double gstPercentage) { this.gstPercentage = gstPercentage; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public String getDiscrepancyType() { return discrepancyType; }
    public void setDiscrepancyType(String discrepancyType) { this.discrepancyType = discrepancyType; }
    public String getDiscrepancySeverity() { return discrepancySeverity; }
    public void setDiscrepancySeverity(String discrepancySeverity) { this.discrepancySeverity = discrepancySeverity; }
}
