package com.pharmadesk.backend.sales.controller;

import com.pharmadesk.backend.sales.model.BillCancellationRequest;
import com.pharmadesk.backend.sales.service.BillCancellationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pharmacy/bills/cancellations")
public class BillCancellationController {

    private final BillCancellationService service;

    public BillCancellationController(BillCancellationService service) {
        this.service = service;
    }

    @PostMapping("/request")
    public ResponseEntity<BillCancellationRequest> requestCancellation(
            @RequestBody Map<String, Object> payload, Authentication auth) {
        Long billId = Long.valueOf(payload.get("billId").toString());
        String reason = payload.get("reason").toString();
        String requestedBy = auth.getName();
        return ResponseEntity.ok(service.requestCancellation(billId, reason, requestedBy));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN', 'ROLE_SUPERVISOR')")
    public ResponseEntity<BillCancellationRequest> approveCancellation(
            @PathVariable Long id, Authentication auth) {
        String reviewedBy = auth.getName();
        return ResponseEntity.ok(service.approveCancellation(id, reviewedBy));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN', 'ROLE_SUPERVISOR')")
    public ResponseEntity<BillCancellationRequest> rejectCancellation(
            @PathVariable Long id, @RequestBody Map<String, String> payload, Authentication auth) {
        String reason = payload.get("reason");
        String reviewedBy = auth.getName();
        return ResponseEntity.ok(service.rejectCancellation(id, reason, reviewedBy));
    }
}
