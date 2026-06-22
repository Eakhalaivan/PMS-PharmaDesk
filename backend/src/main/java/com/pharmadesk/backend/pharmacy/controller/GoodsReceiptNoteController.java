package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.model.GoodsReceiptNote;
import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.service.GoodsReceiptNoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/grns")
public class GoodsReceiptNoteController {

    private final GoodsReceiptNoteService grnService;

    public GoodsReceiptNoteController(GoodsReceiptNoteService grnService) {
        this.grnService = grnService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoodsReceiptNote>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(grnService.getAll(), "GRNs fetched"));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<GoodsReceiptNote>>> getBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(ApiResponse.success(grnService.getBySupplier(supplierId), "GRNs fetched"));
    }

    @GetMapping("/po/{poId}")
    public ResponseEntity<ApiResponse<List<GoodsReceiptNote>>> getByPo(@PathVariable String poId) {
        return ResponseEntity.ok(ApiResponse.success(grnService.getByPo(poId), "GRNs fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoodsReceiptNote>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(grnService.getById(id), "GRN found"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','STOREKEEPER')")
    public ResponseEntity<ApiResponse<GoodsReceiptNote>> create(@RequestBody GoodsReceiptNote grn) {
        return ResponseEntity.ok(ApiResponse.success(grnService.createGrn(grn), "GRN created"));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','STOREKEEPER')")
    public ResponseEntity<ApiResponse<GoodsReceiptNote>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(grnService.confirmGrn(id), "GRN confirmed and stock updated"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','STOREKEEPER')")
    public ResponseEntity<ApiResponse<GoodsReceiptNote>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(grnService.updateStatus(id, status), "Status updated"));
    }
}
