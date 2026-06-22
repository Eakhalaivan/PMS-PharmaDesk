package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goods_receipt_notes")
@SQLDelete(sql = "UPDATE goods_receipt_notes SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class GoodsReceiptNote extends BaseEntity {

    @Column(name = "grn_number", unique = true, nullable = false)
    private String grnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "supplier_invoice_number")
    private String supplierInvoiceNumber;

    @Column(name = "invoice_date")
    private java.time.LocalDate invoiceDate;

    @Column(name = "delivery_challan_number")
    private String deliveryChallanNumber;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "received_by")
    private String receivedBy;

    @Column(name = "received_date")
    private LocalDateTime receivedDate = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "DRAFT"; // DRAFT, CONFIRMED, PARTIALLY_RECEIVED

    @OneToMany(mappedBy = "goodsReceiptNote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptNoteItem> items = new ArrayList<>();

    // Getters and Setters
    public String getGrnNumber() { return grnNumber; }
    public void setGrnNumber(String grnNumber) { this.grnNumber = grnNumber; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public String getSupplierInvoiceNumber() { return supplierInvoiceNumber; }
    public void setSupplierInvoiceNumber(String supplierInvoiceNumber) { this.supplierInvoiceNumber = supplierInvoiceNumber; }
    public java.time.LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(java.time.LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getDeliveryChallanNumber() { return deliveryChallanNumber; }
    public void setDeliveryChallanNumber(String deliveryChallanNumber) { this.deliveryChallanNumber = deliveryChallanNumber; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    public LocalDateTime getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDateTime receivedDate) { this.receivedDate = receivedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<GoodsReceiptNoteItem> getItems() { return items; }
    public void setItems(List<GoodsReceiptNoteItem> items) { this.items = items; }
}
