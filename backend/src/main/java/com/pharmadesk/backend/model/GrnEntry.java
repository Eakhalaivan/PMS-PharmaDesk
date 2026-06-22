package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grn_entries")
public class GrnEntry {

    @Id
    @Column(name = "grn_id", length = 36)
    private String grnId = java.util.UUID.randomUUID().toString();

    @Column(name = "grn_number", nullable = false, unique = true, length = 30)
    private String grnNumber;

    @Column(name = "grn_date", nullable = false)
    private LocalDate grnDate = LocalDate.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "po_id")
    private PurchaseOrder purchaseOrder;

    @Column(name = "po_number", length = 30)
    private String poNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "supplier_name", nullable = false, length = 150)
    private String supplierName;

    @Column(name = "invoice_number", nullable = false, length = 60)
    private String invoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "invoice_value")
    private BigDecimal invoiceValue;

    @Column(name = "delivery_challan_number", length = 60)
    private String deliveryChallanNumber;

    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber;

    @Column(name = "total_received_value")
    private BigDecimal totalReceivedValue = BigDecimal.ZERO;

    @Column(name = "total_rejected_value")
    private BigDecimal totalRejectedValue = BigDecimal.ZERO;

    @Column(name = "net_grn_value")
    private BigDecimal netGrnValue = BigDecimal.ZERO;

    @Column(name = "matching_status", length = 30)
    private String matchingStatus = "pending";

    @Column(name = "discrepancy_notes", columnDefinition = "TEXT")
    private String discrepancyNotes;

    @Column(length = 20)
    private String status = "draft";

    @Column(name = "received_by", nullable = false)
    private Long receivedBy;

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "goodsReceiptNote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GrnLineItem> items = new ArrayList<>();

    // Getters and Setters
    public String getGrnId() { return grnId; }
    public void setGrnId(String grnId) { this.grnId = grnId; }

    public String getGrnNumber() { return grnNumber; }
    public void setGrnNumber(String grnNumber) { this.grnNumber = grnNumber; }

    public LocalDate getGrnDate() { return grnDate; }
    public void setGrnDate(LocalDate grnDate) { this.grnDate = grnDate; }

    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public BigDecimal getInvoiceValue() { return invoiceValue; }
    public void setInvoiceValue(BigDecimal invoiceValue) { this.invoiceValue = invoiceValue; }

    public String getDeliveryChallanNumber() { return deliveryChallanNumber; }
    public void setDeliveryChallanNumber(String deliveryChallanNumber) { this.deliveryChallanNumber = deliveryChallanNumber; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public BigDecimal getTotalReceivedValue() { return totalReceivedValue; }
    public void setTotalReceivedValue(BigDecimal totalReceivedValue) { this.totalReceivedValue = totalReceivedValue; }

    public BigDecimal getTotalRejectedValue() { return totalRejectedValue; }
    public void setTotalRejectedValue(BigDecimal totalRejectedValue) { this.totalRejectedValue = totalRejectedValue; }

    public BigDecimal getNetGrnValue() { return netGrnValue; }
    public void setNetGrnValue(BigDecimal netGrnValue) { this.netGrnValue = netGrnValue; }

    public String getMatchingStatus() { return matchingStatus; }
    public void setMatchingStatus(String matchingStatus) { this.matchingStatus = matchingStatus; }

    public String getDiscrepancyNotes() { return discrepancyNotes; }
    public void setDiscrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getReceivedBy() { return receivedBy; }
    public void setReceivedBy(Long receivedBy) { this.receivedBy = receivedBy; }

    public Long getConfirmedBy() { return confirmedBy; }
    public void setConfirmedBy(Long confirmedBy) { this.confirmedBy = confirmedBy; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public List<GrnLineItem> getItems() { return items; }
    public void setItems(List<GrnLineItem> items) { this.items = items; }
}
