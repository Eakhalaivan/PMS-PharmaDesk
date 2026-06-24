package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.dto.dashboard.*;
import com.pharmadesk.backend.pharmacy.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }



    @GetMapping
    public ApiResponse<DashboardKpiDTO> getDashboardKpis(HttpServletRequest request) {
        String role = getPrimaryRole(request);
        return ApiResponse.success(dashboardService.buildKpisForRole(role), "KPIs fetched successfully");
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
}
