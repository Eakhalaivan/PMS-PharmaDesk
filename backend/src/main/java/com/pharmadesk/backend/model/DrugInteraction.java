package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_interactions")
public class DrugInteraction {

    @Id
    @Column(name = "interaction_id", length = 36)
    private String interactionId = java.util.UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_a_id", nullable = false)
    private Medicine medicineA;

    @Column(name = "medicine_a_name", nullable = false, length = 150)
    private String medicineAName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_b_id", nullable = false)
    private Medicine medicineB;

    @Column(name = "medicine_b_name", nullable = false, length = 150)
    private String medicineBName;

    @Column(nullable = false, length = 20)
    private String severity; // contraindicated, high, moderate, low, informational

    @Column(name = "interaction_type", length = 100)
    private String interactionType;

    @Column(name = "interaction_mechanism", columnDefinition = "TEXT")
    private String interactionMechanism;

    @Column(name = "clinical_consequence", nullable = false, columnDefinition = "TEXT")
    private String clinicalConsequence;

    @Column(name = "recommended_action", nullable = false, columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(length = 50)
    private String onset;

    @Column(name = "documentation_level", nullable = false, length = 20)
    private String documentationLevel;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters and Setters
    public String getInteractionId() { return interactionId; }
    public void setInteractionId(String interactionId) { this.interactionId = interactionId; }

    public Medicine getMedicineA() { return medicineA; }
    public void setMedicineA(Medicine medicineA) { this.medicineA = medicineA; }

    public String getMedicineAName() { return medicineAName; }
    public void setMedicineAName(String medicineAName) { this.medicineAName = medicineAName; }

    public Medicine getMedicineB() { return medicineB; }
    public void setMedicineB(Medicine medicineB) { this.medicineB = medicineB; }

    public String getMedicineBName() { return medicineBName; }
    public void setMedicineBName(String medicineBName) { this.medicineBName = medicineBName; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getInteractionType() { return interactionType; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }

    public String getInteractionMechanism() { return interactionMechanism; }
    public void setInteractionMechanism(String interactionMechanism) { this.interactionMechanism = interactionMechanism; }

    public String getClinicalConsequence() { return clinicalConsequence; }
    public void setClinicalConsequence(String clinicalConsequence) { this.clinicalConsequence = clinicalConsequence; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public String getOnset() { return onset; }
    public void setOnset(String onset) { this.onset = onset; }

    public String getDocumentationLevel() { return documentationLevel; }
    public void setDocumentationLevel(String documentationLevel) { this.documentationLevel = documentationLevel; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
