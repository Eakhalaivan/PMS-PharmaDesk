package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.model.InsuranceClaim;
import com.pharmadesk.backend.model.InsuranceClaimLineItem;
import com.pharmadesk.backend.model.InsuranceProvider;
import com.pharmadesk.backend.pharmacy.repository.InsuranceClaimRepository;
import com.pharmadesk.backend.pharmacy.repository.InsuranceProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pharmadesk.backend.pharmacy.dto.common.PageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class InsuranceClaimService {

    private final InsuranceClaimRepository claimRepository;
    private final InsuranceProviderRepository providerRepository;

    public InsuranceClaimService(InsuranceClaimRepository claimRepository,
                                 InsuranceProviderRepository providerRepository) {
        this.claimRepository = claimRepository;
        this.providerRepository = providerRepository;
    }

    public PageResponse<InsuranceClaim> getAllClaims(Pageable pageable) {
        Page<InsuranceClaim> pageResult = claimRepository.findAll(pageable);
        return new PageResponse<>(pageResult);
    }

    public InsuranceClaim getClaimById(String id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + id));
    }

    @Transactional
    public InsuranceClaim createClaim(InsuranceClaim claim) {
        claim.setClaimId(UUID.randomUUID().toString());
        claim.setClaimNumber("CLM-" + (System.currentTimeMillis() % 100000));
        claim.setClaimDate(LocalDate.now());
        claim.setClaimStatus("draft");
        if (claim.getLineItems() != null) {
            claim.getLineItems().forEach(item -> {
                item.setClaimLineId(UUID.randomUUID().toString());
                item.setInsuranceClaim(claim);
            });
        }
        return claimRepository.save(claim);
    }

    @Transactional
    public InsuranceClaim updateClaimStatus(String claimId, String status) {
        InsuranceClaim claim = getClaimById(claimId);
        claim.setClaimStatus(status);
        if ("approved".equalsIgnoreCase(status)) {
            claim.setApprovalDate(LocalDate.now());
        } else if ("settled".equalsIgnoreCase(status)) {
            claim.setSettlementDate(LocalDate.now());
        }
        return claimRepository.save(claim);
    }

    public PageResponse<InsuranceProvider> getAllProviders(Pageable pageable) {
        Page<InsuranceProvider> pageResult = providerRepository.findAll(pageable);
        return new PageResponse<>(pageResult);
    }

    @Transactional
    public InsuranceProvider createProvider(InsuranceProvider provider) {
        provider.setProviderId(UUID.randomUUID().toString());
        return providerRepository.save(provider);
    }
}
