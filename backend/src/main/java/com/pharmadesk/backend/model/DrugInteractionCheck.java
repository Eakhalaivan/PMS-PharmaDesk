package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_interaction_checks")
public class DrugInteractionCheck {

    @Id
    @Column(name = "check_id", length = 36)
    private String checkId = java.util.UUID.randomUUID().toString();

    @Column(name = "bill_id")
    private Long billId;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "patient_name", length = 100)
    private String patientName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_a_id", nullable = false)
    private Medicine medicineA;

    @Column(name = "medicine_a_name", length = 150)
    private String medicineAName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_b_id", nullable = false)
    private Medicine medicineB;

    @Column(name = "medicine_b_name", length = 150)
    private String medicineBName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interaction_id")
    private DrugInteraction interaction;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(name = "was_overridden")
    private boolean overridden = false;

    @Column(name = "override_reason", columnDefinition = "TEXT")
    private String overrideReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "override_by")
    private User overrideBy;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt = LocalDateTime.now();

    // Getters and Setters
    public String getCheckId() { return checkId; }
    public void setCheckId(String checkId) { this.checkId = checkId; }

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Medicine getMedicineA() { return medicineA; }
    public void setMedicineA(Medicine medicineA) { this.medicineA = medicineA; }

    public String getMedicineAName() { return medicineAName; }
    public void setMedicineAName(String medicineAName) { this.medicineAName = medicineAName; }

    public Medicine getMedicineB() { return medicineB; }
    public void setMedicineB(Medicine medicineB) { this.medicineB = medicineB; }

    public String getMedicineBName() { return medicineBName; }
    public void setMedicineBName(String medicineBName) { this.medicineBName = medicineBName; }

    public DrugInteraction getInteraction() { return interaction; }
    public void setInteraction(DrugInteraction interaction) { this.interaction = interaction; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public boolean isOverridden() { return overridden; }
    public void setOverridden(boolean overridden) { this.overridden = overridden; }

    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }

    public User getOverrideBy() { return overrideBy; }
    public void setOverrideBy(User overrideBy) { this.overrideBy = overrideBy; }

    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
}
