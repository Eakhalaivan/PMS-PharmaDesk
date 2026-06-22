package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "return_to_suppliers")
@SQLDelete(sql = "UPDATE return_to_suppliers SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class ReturnToSupplier extends BaseEntity {

    @Column(name = "return_number", unique = true, nullable = false)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    private GoodsReceiptNote goodsReceiptNote;

    // Near Expiry, Damaged on Receipt, Wrong Item, Excess Stock
    @Column(name = "reason")
    private String reason;

    // INITIATED, DISPATCHED, CREDIT_NOTE_RECEIVED, SETTLED
    @Column(nullable = false)
    private String status = "INITIATED";

    @Column(name = "expected_credit_value")
    private BigDecimal expectedCreditValue;

    @Column(name = "actual_credit_value")
    private BigDecimal actualCreditValue;

    @Column(name = "transport_details", length = 500)
    private String transportDetails;

    @Column(name = "credit_note_number")
    private String creditNoteNumber;

    @OneToMany(mappedBy = "returnToSupplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnToSupplierItem> items = new ArrayList<>();

    // Getters and Setters
    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public GoodsReceiptNote getGoodsReceiptNote() { return goodsReceiptNote; }
    public void setGoodsReceiptNote(GoodsReceiptNote goodsReceiptNote) { this.goodsReceiptNote = goodsReceiptNote; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getExpectedCreditValue() { return expectedCreditValue; }
    public void setExpectedCreditValue(BigDecimal expectedCreditValue) { this.expectedCreditValue = expectedCreditValue; }
    public BigDecimal getActualCreditValue() { return actualCreditValue; }
    public void setActualCreditValue(BigDecimal actualCreditValue) { this.actualCreditValue = actualCreditValue; }
    public String getTransportDetails() { return transportDetails; }
    public void setTransportDetails(String transportDetails) { this.transportDetails = transportDetails; }
    public String getCreditNoteNumber() { return creditNoteNumber; }
    public void setCreditNoteNumber(String creditNoteNumber) { this.creditNoteNumber = creditNoteNumber; }
    public List<ReturnToSupplierItem> getItems() { return items; }
    public void setItems(List<ReturnToSupplierItem> items) { this.items = items; }
}
