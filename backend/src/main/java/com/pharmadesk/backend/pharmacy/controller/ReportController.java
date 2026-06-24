package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pharmacy/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // ─── SALES ────────────────────────────────────────────────────────────────

    @GetMapping("/sales")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getSalesReport(from, to), "Sales report"));
    }

    @GetMapping("/sales/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailySalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDailySalesSummary(from, to), "Daily summary"));
    }

    @GetMapping("/sales/itemised")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getItemisedRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getItemisedSalesRegister(from, to), "Itemised register"));
    }

    @GetMapping("/sales/medicine-wise")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMedicineWiseSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getMedicineWiseSales(from, to), "Medicine-wise sales"));
    }

    @GetMapping("/sales/credit")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_PHARMACIST','ROLE_ACCOUNTS')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCreditSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getCreditSalesReport(from, to), "Credit sales"));
    }

    @GetMapping("/sales/cancelled")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCancelledBills(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getCancelledBillsReport(from, to), "Cancelled bills"));
    }

    // ─── STOCK ─────────────────────────────────────────────────────────────────

    @GetMapping("/stock")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_STOREKEEPER','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStockReport() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getStockReport(), "Stock report"));
    }

    @GetMapping("/stock/expiry")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_STOREKEEPER','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getExpiryReport(
            @RequestParam(defaultValue = "60") int days) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getExpiryReport(days), "Expiry report"));
    }

    @GetMapping("/stock/slow-moving")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_STOREKEEPER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSlowMoving(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "5") int threshold) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getSlowMovingStockReport(from, to, threshold), "Slow-moving stock"));
    }

    // ─── TAX / GST ──────────────────────────────────────────────────────────────

    @GetMapping("/tax")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_ACCOUNTS')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaxReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getTaxReport(from, to), "Tax/GST report"));
    }

    @GetMapping("/gst/sales")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_ACCOUNTS')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGstSalesRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getGstSalesRegister(from, to), "GST Sales Register"));
    }

    // ─── PURCHASE ──────────────────────────────────────────────────────────────

    @GetMapping("/purchase/register")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_STOREKEEPER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPurchaseRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPurchaseRegister(from, to), "Purchase register"));
    }

    @GetMapping("/purchase/payables")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_ACCOUNTS')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOutstandingPayables() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getOutstandingPayables(), "Outstanding payables"));
    }

    @GetMapping("/supplier/performance")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSupplierPerformance() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getSupplierPerformanceSummary(), "Supplier performance"));
    }
}
