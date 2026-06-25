package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.dto.dashboard.*;
import com.pharmadesk.backend.pharmacy.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pharmacy/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final com.pharmadesk.backend.pharmacy.repository.MedicineStockRepository stockRepository;

    public DashboardController(DashboardService dashboardService,
                               com.pharmadesk.backend.pharmacy.repository.MedicineStockRepository stockRepository) {
        this.dashboardService = dashboardService;
        this.stockRepository = stockRepository;
    }



    @GetMapping
    public ApiResponse<DashboardKpiDTO> getDashboardKpis(HttpServletRequest request) {
        String role = getPrimaryRole(request);
        return ApiResponse.success(dashboardService.buildKpisForRole(role), "KPIs fetched successfully");
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> getDashboardSummary(
            @RequestParam(defaultValue = "7") int days,
            HttpServletRequest request) {
        String role = getPrimaryRole(request);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("kpiData", dashboardService.buildKpisForRole(role));
        summary.put("chartData", dashboardService.getChartData(role, days));
        summary.put("alerts", dashboardService.getAlerts());
        summary.put("revenueStrip", dashboardService.getRevenueStrip());
        return ApiResponse.success(summary, "Dashboard summary fetched successfully");
    }

    @GetMapping("/chart-data")
    public ApiResponse<List<ChartDataPointDTO>> getChartData(
            @RequestParam(defaultValue = "7") int days,
            HttpServletRequest request) {
        String role = getPrimaryRole(request);
        return ApiResponse.success(dashboardService.getChartData(role, days), "Chart data fetched successfully");
    }

    private String getPrimaryRole(HttpServletRequest request) {
        String activeRole = request.getHeader("X-Active-Role");
        if (activeRole != null && !activeRole.isBlank()) return activeRole;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "SYSTEM_ADMIN";
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst().orElse("SYSTEM_ADMIN");
    }

    @GetMapping("/alerts")
    public ApiResponse<List<SystemAlertDTO>> getAlerts() {
        return ApiResponse.success(dashboardService.getAlerts(), "Alerts fetched successfully");
    }

    @GetMapping("/revenue-strip")
    public ApiResponse<RevenueStripDTO> getRevenueStrip() {
        return ApiResponse.success(dashboardService.getRevenueStrip(), "Revenue strip fetched successfully");
    }

    @GetMapping("/recent-activities")
    public ApiResponse<List<ActivityDTO>> getRecentActivities(@RequestParam(defaultValue = "10") int limit) {
        // Return mock data for activities to satisfy endpoint
        List<ActivityDTO> activities = new ArrayList<>();
        return ApiResponse.success(activities, "Recent activities fetched");
    }

    @GetMapping("/low-stock")
    public com.pharmadesk.backend.pharmacy.dto.ApiResponse<java.util.List<java.util.Map<String, Object>>> getLowStockForDashboard() {
        java.util.List<Object[]> rows = stockRepository.findLowStockWithMedicine();
        java.util.List<java.util.Map<String, Object>> result = rows.stream().map(row -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("medicineName",     row[0]);
            m.put("category",         row[1]);
            m.put("quantityAvailable", row[2]);
            m.put("reorderLevel",     row[3]);
            m.put("unit",             row[4]);
            return m;
        }).collect(java.util.stream.Collectors.toList());
        return com.pharmadesk.backend.pharmacy.dto.ApiResponse.success(result, "Low stock items fetched");
    }
}
