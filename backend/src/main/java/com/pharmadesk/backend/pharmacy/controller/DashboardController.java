package com.pharmadesk.backend.pharmacy.controller;

import com.pharmadesk.backend.pharmacy.dto.ApiResponse;
import com.pharmadesk.backend.pharmacy.dto.dashboard.*;
import com.pharmadesk.backend.pharmacy.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    private String getPrimaryRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "SYSTEM_ADMIN";
        }
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("SYSTEM_ADMIN");
    }

    @GetMapping
    public ApiResponse<DashboardKpiDTO> getDashboardKpis() {
        String role = getPrimaryRole();
        return ApiResponse.success(dashboardService.buildKpisForRole(role), "KPIs fetched successfully");
    }

    @GetMapping("/chart-data")
    public ApiResponse<List<ChartDataPointDTO>> getChartData(@RequestParam(defaultValue = "SYSTEM_ADMIN") String role,
                                                             @RequestParam(defaultValue = "7") int days) {
        // Here we could use the passed role, or force the primary role
        return ApiResponse.success(dashboardService.getChartData(role, days), "Chart data fetched successfully");
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
