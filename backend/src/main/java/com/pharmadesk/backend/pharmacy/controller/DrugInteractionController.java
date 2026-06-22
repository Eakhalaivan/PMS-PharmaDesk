package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.model.DrugInteraction;
import com.pharmadesk.backend.model.DrugInteractionCheck;
import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.service.DrugInteractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drug-interactions")
public class DrugInteractionController {

    private final DrugInteractionService service;

    public DrugInteractionController(DrugInteractionService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<List<DrugInteraction>>> check(@RequestBody List<Long> medicineIds) {
        return ResponseEntity.ok(ApiResponse.success(service.checkInteractions(medicineIds), "Interaction check complete"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DrugInteraction>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllInteractions(), "Interactions fetched"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<DrugInteraction>> create(@RequestBody DrugInteraction interaction) {
        return ResponseEntity.ok(ApiResponse.success(service.createInteraction(interaction), "Adverse interaction rule added"));
    }

    @PostMapping("/log-check")
    public ResponseEntity<ApiResponse<DrugInteractionCheck>> logCheck(@RequestBody DrugInteractionCheck check) {
        return ResponseEntity.ok(ApiResponse.success(service.logInteractionCheck(check), "Interaction audit log recorded"));
    }

    @GetMapping("/incident-report")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','AUDIT_COMPLIANCE','SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<DrugInteractionCheck>>> getIncidents() {
        return ResponseEntity.ok(ApiResponse.success(service.getIncidentReport(), "Interaction overrides and checks logged"));
    }
}
