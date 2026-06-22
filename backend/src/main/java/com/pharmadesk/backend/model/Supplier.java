package com.pharmadesk.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "suppliers")
@SQLDelete(sql = "UPDATE suppliers SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class Supplier extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String contact;
    private String gstin;
    private String address;

    @Column(nullable = false, unique = true)
    private String supplierCode;
    private String supplierType;
    private String contactPersonName;
    private String designation;
    private String mobileNumber;
    private String alternatePhone;
    private String emailAddress;
    private String pincode;
    private String city;
    private String state;
    private String country;
    private String drugLicenseNumber;
    private java.time.LocalDate drugLicenseExpiry;
    private String panNumber;

    // Bank Account Details
    private String accountNumber;
    private String bankName;
    private String branch;
    private String ifscCode;

    private String paymentTerms;
    private Double creditLimit;
    private String preferredDeliveryDays;
    private Integer averageLeadTime;
    private String status;

    @Column(length = 1000)
    private String notes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierType() { return supplierType; }
    public void setSupplierType(String supplierType) { this.supplierType = supplierType; }
    public String getContactPersonName() { return contactPersonName; }
    public void setContactPersonName(String contactPersonName) { this.contactPersonName = contactPersonName; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getDrugLicenseNumber() { return drugLicenseNumber; }
    public void setDrugLicenseNumber(String drugLicenseNumber) { this.drugLicenseNumber = drugLicenseNumber; }
    public java.time.LocalDate getDrugLicenseExpiry() { return drugLicenseExpiry; }
    public void setDrugLicenseExpiry(java.time.LocalDate drugLicenseExpiry) { this.drugLicenseExpiry = drugLicenseExpiry; }
    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    public Double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(Double creditLimit) { this.creditLimit = creditLimit; }
    public String getPreferredDeliveryDays() { return preferredDeliveryDays; }
    public void setPreferredDeliveryDays(String preferredDeliveryDays) { this.preferredDeliveryDays = preferredDeliveryDays; }
    public Integer getAverageLeadTime() { return averageLeadTime; }
    public void setAverageLeadTime(Integer averageLeadTime) { this.averageLeadTime = averageLeadTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
