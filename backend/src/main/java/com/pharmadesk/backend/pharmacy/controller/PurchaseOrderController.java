package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.model.PurchaseOrder;
import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.service.PurchaseOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/purchase-orders", "/api/pharmacy/purchase-orders"})
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    public PurchaseOrderController(PurchaseOrderService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseOrder>>> getPOs(@RequestParam(value = "status", required = false) String status) {
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            String dbStatus = status.toLowerCase();
            if (status.equalsIgnoreCase("PENDING")) {
                dbStatus = "submitted";
            } else if (status.equalsIgnoreCase("RECEIVED")) {
                dbStatus = "completed";
            }
            
            java.util.List<String> validStatuses = java.util.List.of("draft", "submitted", "approved", "sent", "partially_received", "completed", "cancelled");
            if (!validStatuses.contains(dbStatus)) {
                return ResponseEntity.ok(ApiResponse.success(java.util.List.of(), "No POs found for status: " + status));
            }
            return ResponseEntity.ok(ApiResponse.success(service.getPOsByStatus(dbStatus), "POs fetched"));
        }
        return ResponseEntity.ok(ApiResponse.success(service.getAllPos(), "POs fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrder>> getPO(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getPoById(id), "PO details"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','STOREKEEPER','PURCHASE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> create(@RequestBody PurchaseOrder po) {
        return ResponseEntity.ok(ApiResponse.success(service.createPO(po), "PO created successfully"));
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','STOREKEEPER','PURCHASE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> submit(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.submitForApproval(id), "PO submitted"));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SUPERVISOR','PURCHASE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> approve(@PathVariable String id, @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(service.approvePO(id, userId), "PO approved"));
    }

    @PutMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PURCHASE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> send(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.sendToSupplier(id), "PO sent to supplier"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PURCHASE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> cancel(@PathVariable String id, @RequestParam String reason, @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(service.cancelPO(id, reason, userId), "PO cancelled"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(service.getPoSummary(), "PO summary stats"));
    }
}
