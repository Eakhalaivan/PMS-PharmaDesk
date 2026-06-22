package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "narcotic_register")
public class NarcoticRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long entryId;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Column(name = "entry_date")
    private LocalDateTime entryDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = false)
    private MedicineStock batch;

    @Column(name = "patient_full_name", nullable = false)
    private String patientFullName;

    @Column(name = "patient_age")
    private Integer patientAge;

    @Column(name = "patient_gender")
    private String patientGender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "ward_name")
    private String wardName;

    @Column(name = "bed_number")
    private String bedNumber;

    @Column(name = "doctor_name")
    private String doctorName;

    @Column(name = "doctor_registration_number")
    private String doctorRegistrationNumber;

    @Column(name = "prescription_number")
    private String prescriptionNumber;

    @Column(name = "prescription_date")
    private LocalDate prescriptionDate;

    @Column(name = "quantity_prescribed", nullable = false)
    private Integer quantityPrescribed;

    @Column(name = "quantity_dispensed", nullable = false)
    private Integer quantityDispensed;

    @Column(name = "opening_balance", nullable = false)
    private Integer openingBalance;

    @Column(name = "closing_balance", nullable = false)
    private Integer closingBalance;

    @Column(name = "dispensing_pharmacist_id")
    private Long dispensingPharmacistId;

    @Column(name = "digital_acknowledgment_timestamp")
    private LocalDateTime digitalAcknowledgmentTimestamp;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "discrepancy_flag")
    private boolean discrepancyFlag = false;

    // Getters and Setters
    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) { this.entryId = entryId; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public LocalDateTime getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDateTime entryDate) { this.entryDate = entryDate; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public MedicineStock getBatch() { return batch; }
    public void setBatch(MedicineStock batch) { this.batch = batch; }

    public String getPatientFullName() { return patientFullName; }
    public void setPatientFullName(String patientFullName) { this.patientFullName = patientFullName; }

    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorRegistrationNumber() { return doctorRegistrationNumber; }
    public void setDoctorRegistrationNumber(String doctorRegistrationNumber) { this.doctorRegistrationNumber = doctorRegistrationNumber; }

    public String getPrescriptionNumber() { return prescriptionNumber; }
    public void setPrescriptionNumber(String prescriptionNumber) { this.prescriptionNumber = prescriptionNumber; }

    public LocalDate getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }

    public Integer getQuantityPrescribed() { return quantityPrescribed; }
    public void setQuantityPrescribed(Integer quantityPrescribed) { this.quantityPrescribed = quantityPrescribed; }

    public Integer getQuantityDispensed() { return quantityDispensed; }
    public void setQuantityDispensed(Integer quantityDispensed) { this.quantityDispensed = quantityDispensed; }

    public Integer getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(Integer openingBalance) { this.openingBalance = openingBalance; }

    public Integer getClosingBalance() { return closingBalance; }
    public void setClosingBalance(Integer closingBalance) { this.closingBalance = closingBalance; }

    public Long getDispensingPharmacistId() { return dispensingPharmacistId; }
    public void setDispensingPharmacistId(Long dispensingPharmacistId) { this.dispensingPharmacistId = dispensingPharmacistId; }

    public LocalDateTime getDigitalAcknowledgmentTimestamp() { return digitalAcknowledgmentTimestamp; }
    public void setDigitalAcknowledgmentTimestamp(LocalDateTime digitalAcknowledgmentTimestamp) { this.digitalAcknowledgmentTimestamp = digitalAcknowledgmentTimestamp; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public boolean isDiscrepancyFlag() { return discrepancyFlag; }
    public void setDiscrepancyFlag(boolean discrepancyFlag) { this.discrepancyFlag = discrepancyFlag; }
}
