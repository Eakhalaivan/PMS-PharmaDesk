package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.dto.analytics.AnalyticsDashboardDTO;
import com.pharmadesk.backend.pharmacy.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<ApiResponse<AnalyticsDashboardDTO>> getDashboardSummary(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        AnalyticsDashboardDTO summary = analyticsService.getDashboardSummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary retrieved successfully"));
    }

    @GetMapping("/abc-analysis")
    public ResponseEntity<ApiResponse<java.util.List<com.pharmadesk.backend.pharmacy.dto.analytics.ABCAnalysisDTO>>> getAbcAnalysis(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        java.util.List<com.pharmadesk.backend.pharmacy.dto.analytics.ABCAnalysisDTO> data = analyticsService.getAbcAnalysis(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(data, "ABC Analysis retrieved successfully"));
    }

    @GetMapping("/mom-comparison")
    public ResponseEntity<ApiResponse<com.pharmadesk.backend.pharmacy.dto.analytics.MonthOverMonthDTO>> getMonthOverMonthComparison(
            @RequestParam("monthAStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthAStart,
            @RequestParam("monthAEnd") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthAEnd,
            @RequestParam("monthBStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthBStart,
            @RequestParam("monthBEnd") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthBEnd) {
        
        com.pharmadesk.backend.pharmacy.dto.analytics.MonthOverMonthDTO data = analyticsService.getMonthOverMonthComparison(monthAStart, monthAEnd, monthBStart, monthBEnd);
        return ResponseEntity.ok(ApiResponse.success(data, "Month over Month comparison retrieved successfully"));
    }
}
