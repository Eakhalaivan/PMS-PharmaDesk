package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.service.ReportService;
import com.pharmadesk.backend.pharmacy.dto.common.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    private void validateDateRange(LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be before 'to' date.");
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(from, to) > 90) {
            throw new IllegalArgumentException("Date range cannot exceed 90 days.");
        }
    }

    // ─── SALES ────────────────────────────────────────────────────────────────

    @GetMapping("/sales")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<PageResponse<?>>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "billingDate,desc") String[] sort) {
        validateDateRange(from, to);
        Sort.Direction dir = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(dir, sort[0]));
        return ResponseEntity.ok(ApiResponse.success(reportService.getSalesReport(from, to, pageRequest), "Sales report"));
    }

    @GetMapping("/sales/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<Object>> getDailySalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        validateDateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success(reportService.getDailySalesSummary(from, to), "Daily summary"));
    }

    @GetMapping("/sales/itemised")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<List<?>>> getItemisedRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        validateDateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success(reportService.getItemisedSalesRegister(from, to), "Itemised register"));
    }

    @GetMapping("/sales/medicine-wise")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<List<?>>> getMedicineWiseSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        validateDateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success(reportService.getMedicineWiseSales(from, to), "Medicine-wise sales"));
    }

    @GetMapping("/sales/credit")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_PHARMACIST','ROLE_ACCOUNTS')")
    public ResponseEntity<ApiResponse<List<?>>> getCreditSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        validateDateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success(reportService.getCreditSalesReport(from, to), "Credit sales"));
    }

    @GetMapping("/sales/cancelled")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<?>>> getCancelledBills(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        validateDateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success(reportService.getCancelledBillsReport(from, to), "Cancelled bills"));
    }

    // ─── STOCK ─────────────────────────────────────────────────────────────────

    @GetMapping("/stock")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_STOREKEEPER','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<PageResponse<?>>> getStockReport(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "medicine.name,asc") String[] sort) {
        Sort.Direction dir = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(dir, sort[0]));
        return ResponseEntity.ok(ApiResponse.success(reportService.getStockReport(search, pageRequest), "Stock report"));
    }

    @GetMapping("/stock/expiry")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_STOREKEEPER','ROLE_PHARMACIST')")
    public ResponseEntity<ApiResponse<List<?>>> getExpiryReport(
            @RequestParam(defaultValue = "60") int days) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getExpiryReport(days), "Expiry report"));
    }

    @GetMapping("/stock/slow-moving")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_STOREKEEPER')")
    public ResponseEntity<ApiResponse<List<?>>> getSlowMoving(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "5") int threshold) {
        validateDateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success(reportService.getSlowMovingStockReport(from, to, threshold), "Slow-moving stock"));
    }

    // ─── TAX / GST ──────────────────────────────────────────────────────────────

    @GetMapping("/tax")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_ACCOUNTS')")
    public ResponseEntity<ApiResponse<Object>> getTaxReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        validateDateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success(reportService.getTaxReport(from, to), "Tax/GST report"));
    }

    @GetMapping("/gst/sales")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_ACCOUNTS')")
    public ResponseEntity<ApiResponse<List<?>>> getGstSalesRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        validateDateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success(reportService.getGstSalesRegister(from, to), "GST Sales Register"));
    }

    // ─── PURCHASE ──────────────────────────────────────────────────────────────

    @GetMapping("/purchase/register")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR','ROLE_STOREKEEPER')")
    public ResponseEntity<ApiResponse<List<?>>> getPurchaseRegister(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        validateDateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success(reportService.getPurchaseRegister(from, to), "Purchase register"));
    }

    @GetMapping("/purchase/payables")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_ACCOUNTS')")
    public ResponseEntity<ApiResponse<List<?>>> getOutstandingPayables() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getOutstandingPayables(), "Outstanding payables"));
    }

    @GetMapping("/supplier/performance")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<?>>> getSupplierPerformance() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getSupplierPerformanceSummary(), "Supplier performance"));
    }
}
