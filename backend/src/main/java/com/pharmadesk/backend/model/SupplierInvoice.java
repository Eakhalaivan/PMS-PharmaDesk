package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supplier_invoices")
@SQLDelete(sql = "UPDATE supplier_invoices SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class SupplierInvoice extends BaseEntity {

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id")
    private GoodsReceiptNote goodsReceiptNote;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    // PENDING_MATCH, MATCHED, DISPUTED, PAID
    @Column(nullable = false)
    private String status = "PENDING_MATCH";

    @Column(name = "gstin_on_invoice")
    private String gstinOnInvoice;

    @Column(name = "match_notes", length = 1000)
    private String matchNotes;

    @OneToMany(mappedBy = "supplierInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierInvoiceItem> items = new ArrayList<>();

    // Getters and Setters
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public GoodsReceiptNote getGoodsReceiptNote() { return goodsReceiptNote; }
    public void setGoodsReceiptNote(GoodsReceiptNote goodsReceiptNote) { this.goodsReceiptNote = goodsReceiptNote; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGstinOnInvoice() { return gstinOnInvoice; }
    public void setGstinOnInvoice(String gstinOnInvoice) { this.gstinOnInvoice = gstinOnInvoice; }
    public String getMatchNotes() { return matchNotes; }
    public void setMatchNotes(String matchNotes) { this.matchNotes = matchNotes; }
    public List<SupplierInvoiceItem> getItems() { return items; }
    public void setItems(List<SupplierInvoiceItem> items) { this.items = items; }
}
