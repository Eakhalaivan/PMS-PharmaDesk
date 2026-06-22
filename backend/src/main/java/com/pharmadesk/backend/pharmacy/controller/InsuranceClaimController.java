package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.model.InsuranceClaim;
import com.pharmadesk.backend.model.InsuranceProvider;
import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.service.InsuranceClaimService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insurance-claims")
public class InsuranceClaimController {

    private final InsuranceClaimService service;

    public InsuranceClaimController(InsuranceClaimService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InsuranceClaim>>> getClaims() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllClaims(), "Claims fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InsuranceClaim>> getClaim(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getClaimById(id), "Claim details"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','BILLING_STAFF','CASHIER')")
    public ResponseEntity<ApiResponse<InsuranceClaim>> create(@RequestBody InsuranceClaim claim) {
        return ResponseEntity.ok(ApiResponse.success(service.createClaim(claim), "Claim draft created successfully"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ACCOUNTS_MANAGER','SUPERVISOR')")
    public ResponseEntity<ApiResponse<InsuranceClaim>> updateStatus(@PathVariable String id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(service.updateClaimStatus(id, status), "Claim status updated"));
    }

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<InsuranceProvider>>> getProviders() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllProviders(), "Insurance providers fetched"));
    }

    @PostMapping("/providers")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<InsuranceProvider>> createProvider(@RequestBody InsuranceProvider provider) {
        return ResponseEntity.ok(ApiResponse.success(service.createProvider(provider), "Insurance provider configured"));
    }
}
