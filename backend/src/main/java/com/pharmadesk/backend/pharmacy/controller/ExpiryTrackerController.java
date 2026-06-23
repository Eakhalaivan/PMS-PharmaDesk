package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.model.BatchReturnToSupplier;
import com.pharmadesk.backend.model.StockBatch;
import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.service.ExpiryTrackerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expiry-tracker")
public class ExpiryTrackerController {

    private final ExpiryTrackerService service;

    public ExpiryTrackerController(ExpiryTrackerService service) {
        this.service = service;
    }

    @GetMapping("/batches")
    public ResponseEntity<ApiResponse<List<StockBatch>>> getBatches() {
        return ResponseEntity.ok(ApiResponse.success(service.getFefoStockView(), "Batches fetched successfully"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(service.getExpirySummary(), "Expiry summary fetched"));
    }

    @PostMapping("/return")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_PHARMACY_STAFF','ROLE_STOREKEEPER')")
    public ResponseEntity<ApiResponse<BatchReturnToSupplier>> initiateReturn(@RequestBody BatchReturnToSupplier returnRequest) {
        return ResponseEntity.ok(ApiResponse.success(service.initiateBatchReturn(returnRequest), "Return initiated successfully"));
    }

    @PutMapping("/return/{returnId}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_STOREKEEPER')")
    public ResponseEntity<ApiResponse<BatchReturnToSupplier>> updateStatus(@PathVariable String returnId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(service.updateReturnStatus(returnId, status), "Status updated"));
    }

    @GetMapping("/returns")
    public ResponseEntity<ApiResponse<List<BatchReturnToSupplier>>> getReturns() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllReturns(), "Return records fetched"));
    }
}
