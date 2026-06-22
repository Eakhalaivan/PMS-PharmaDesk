package com.pharmadesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "purchase_order_items")
@SQLDelete(sql = "UPDATE purchase_order_items SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class PurchaseOrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @JsonIgnore
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "estimated_unit_price")
    private java.math.BigDecimal estimatedUnitPrice;

    @Column(name = "last_purchase_price")
    private java.math.BigDecimal lastPurchasePrice;

    @Column(name = "negotiated_price")
    private java.math.BigDecimal negotiatedPrice;

    @Column(name = "gst_percentage")
    private Double gstPercentage;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(name = "line_total")
    private java.math.BigDecimal lineTotal;

    // Getters and Setters
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public java.math.BigDecimal getEstimatedUnitPrice() { return estimatedUnitPrice; }
    public void setEstimatedUnitPrice(java.math.BigDecimal estimatedUnitPrice) { this.estimatedUnitPrice = estimatedUnitPrice; }
    public java.math.BigDecimal getLastPurchasePrice() { return lastPurchasePrice; }
    public void setLastPurchasePrice(java.math.BigDecimal lastPurchasePrice) { this.lastPurchasePrice = lastPurchasePrice; }
    public java.math.BigDecimal getNegotiatedPrice() { return negotiatedPrice; }
    public void setNegotiatedPrice(java.math.BigDecimal negotiatedPrice) { this.negotiatedPrice = negotiatedPrice; }
    public Double getGstPercentage() { return gstPercentage; }
    public void setGstPercentage(Double gstPercentage) { this.gstPercentage = gstPercentage; }
    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }
    public java.math.BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(java.math.BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
