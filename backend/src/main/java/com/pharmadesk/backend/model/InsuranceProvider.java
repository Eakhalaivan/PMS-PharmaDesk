package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_providers")
public class InsuranceProvider {

    @Id
    @Column(name = "provider_id", length = 36)
    private String providerId = java.util.UUID.randomUUID().toString();

    @Column(name = "provider_name", nullable = false, length = 150)
    private String providerName;

    @Column(name = "provider_code", nullable = false, unique = true, length = 20)
    private String providerCode;

    @Column(name = "provider_type", nullable = false, length = 30)
    private String providerType;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(length = 15)
    private String mobile;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "claim_submission_mode", nullable = false, length = 30)
    private String claimSubmissionMode;

    @Column(name = "claim_portal_url", length = 200)
    private String claimPortalUrl;

    @Column(name = "turnaround_days")
    private Integer turnaroundDays = 30;

    @Column(name = "coverage_policy", columnDefinition = "TEXT")
    private String coveragePolicy;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }

    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getClaimSubmissionMode() { return claimSubmissionMode; }
    public void setClaimSubmissionMode(String claimSubmissionMode) { this.claimSubmissionMode = claimSubmissionMode; }

    public String getClaimPortalUrl() { return claimPortalUrl; }
    public void setClaimPortalUrl(String claimPortalUrl) { this.claimPortalUrl = claimPortalUrl; }

    public Integer getTurnaroundDays() { return turnaroundDays; }
    public void setTurnaroundDays(Integer turnaroundDays) { this.turnaroundDays = turnaroundDays; }

    public String getCoveragePolicy() { return coveragePolicy; }
    public void setCoveragePolicy(String coveragePolicy) { this.coveragePolicy = coveragePolicy; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
