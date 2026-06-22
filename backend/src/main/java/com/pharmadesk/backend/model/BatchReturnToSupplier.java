package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_return_to_supplier")
public class BatchReturnToSupplier {

    @Id
    @Column(name = "return_id", length = 36)
    private String returnId = java.util.UUID.randomUUID().toString();

    @Column(name = "batch_id", nullable = false, length = 36)
    private String batchId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "returned_quantity", nullable = false)
    private Integer returnedQuantity;

    @Column(name = "return_reason", nullable = false, length = 30)
    private String returnReason;

    @Column(name = "expected_credit_value")
    private BigDecimal expectedCreditValue;

    @Column(name = "return_status", length = 30)
    private String returnStatus = "initiated";

    @Column(name = "dispatch_date")
    private LocalDate dispatchDate;

    @Column(name = "courier_details", columnDefinition = "TEXT")
    private String courierDetails;

    @Column(name = "credit_note_number", length = 50)
    private String creditNoteNumber;

    @Column(name = "credit_note_date")
    private LocalDate creditNoteDate;

    @Column(name = "actual_credit_value")
    private BigDecimal actualCreditValue;

    @Column(name = "initiated_by", nullable = false)
    private Long initiatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters and Setters
    public String getReturnId() { return returnId; }
    public void setReturnId(String returnId) { this.returnId = returnId; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public Integer getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(Integer returnedQuantity) { this.returnedQuantity = returnedQuantity; }

    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }

    public BigDecimal getExpectedCreditValue() { return expectedCreditValue; }
    public void setExpectedCreditValue(BigDecimal expectedCreditValue) { this.expectedCreditValue = expectedCreditValue; }

    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }

    public LocalDate getDispatchDate() { return dispatchDate; }
    public void setDispatchDate(LocalDate dispatchDate) { this.dispatchDate = dispatchDate; }

    public String getCourierDetails() { return courierDetails; }
    public void setCourierDetails(String courierDetails) { this.courierDetails = courierDetails; }

    public String getCreditNoteNumber() { return creditNoteNumber; }
    public void setCreditNoteNumber(String creditNoteNumber) { this.creditNoteNumber = creditNoteNumber; }

    public LocalDate getCreditNoteDate() { return creditNoteDate; }
    public void setCreditNoteDate(LocalDate creditNoteDate) { this.creditNoteDate = creditNoteDate; }

    public BigDecimal getActualCreditValue() { return actualCreditValue; }
    public void setActualCreditValue(BigDecimal actualCreditValue) { this.actualCreditValue = actualCreditValue; }

    public Long getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(Long initiatedBy) { this.initiatedBy = initiatedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
