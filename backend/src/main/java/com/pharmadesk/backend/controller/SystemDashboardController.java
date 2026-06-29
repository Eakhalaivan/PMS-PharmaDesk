package com.pharmadesk.backend.controller;

import com.pharmadesk.backend.pharmacy.repository.*;
import com.pharmadesk.backend.sales.repository.*;
import com.pharmadesk.backend.repository.ActivityLogRepository;
import com.pharmadesk.backend.repository.UserRepository;
import com.pharmadesk.backend.pharmacy.enums.PrescriptionStatus;
import com.pharmadesk.backend.pharmacy.enums.PaymentStatus;
import com.pharmadesk.backend.pharmacy.enums.ReturnStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class SystemDashboardController {

    private final PharmacyBillRepository billRepository;
    private final MedicineStockRepository stockRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineReturnRepository returnRepository;
    private final CreditBillRepository creditBillRepository;
    private final UserRepository userRepository;

    public SystemDashboardController(PharmacyBillRepository billRepository,
                                     MedicineStockRepository stockRepository,
                                     MedicineRepository medicineRepository,
                                     PrescriptionRepository prescriptionRepository,
                                     MedicineReturnRepository returnRepository,
                                     CreditBillRepository creditBillRepository,
                                     UserRepository userRepository) {
        this.billRepository = billRepository;
        this.stockRepository = stockRepository;
        this.medicineRepository = medicineRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.returnRepository = returnRepository;
        this.creditBillRepository = creditBillRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/kpis")
    public Map<String, Object> getDashboardKPIs() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().atTime(23, 59, 59);

        BigDecimal todaysRevenue = billRepository.sumNetAmountByBillingDateBetween(startOfDay, endOfDay);
        long billsToday          = billRepository.countByBillingDateBetween(startOfDay, endOfDay);
        long pendingRx           = prescriptionRepository.countByStatus(PrescriptionStatus.PENDING.name());
        long lowStockCount       = stockRepository.countLowStockItems();
        long activeStaff         = userRepository.countByStatus("ACTIVE");
        long expiringIn30        = stockRepository.countExpiringWithinDays(30);
        long activePatients      = billRepository.countDistinctPatientNamesToday(startOfDay, endOfDay);

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("total_skus_in_stock",        medicineRepository.count());
        kpis.put("todays_sales_revenue",        todaysRevenue != null ? todaysRevenue : BigDecimal.ZERO);
        kpis.put("bills_today",                 billsToday);
        kpis.put("pending_prescriptions_count", pendingRx);
        kpis.put("low_stock_alerts_count",      lowStockCount);
        kpis.put("expiring_in_30_days_count",   expiringIn30);
        kpis.put("active_patients_today_count", activePatients);
        kpis.put("active_staff",                activeStaff);
        return kpis;
    }

    @GetMapping("/revenue-trend")
    public List<Map<String, Object>> getRevenueTrend(@RequestParam(defaultValue = "7") int days) {
        LocalDateTime from = LocalDate.now().minusDays(days - 1).atStartOfDay();
        LocalDateTime to   = LocalDate.now().atTime(23, 59, 59);
        // Fetch bills in range and aggregate by day in Java (TiDB-safe, no recursive CTE needed)
        return billRepository.findByBillingDateBetween(from, to).stream()
                .filter(b -> b.getBillingDate() != null)
                .collect(Collectors.groupingBy(b -> b.getBillingDate().toLocalDate()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("sale_date",    e.getKey().toString());
                    m.put("day_of_week",  e.getKey().getDayOfWeek().name().substring(0, 3));
                    m.put("daily_revenue", e.getValue().stream()
                            .map(b -> b.getNetAmount() != null ? b.getNetAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    m.put("bill_count",   e.getValue().size());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/revenue-summary")
    public Map<String, Object> getRevenueSummary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd   = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime weekStart  = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        BigDecimal todaysTotal  = billRepository.sumNetAmountByBillingDateBetween(todayStart, todayEnd);
        BigDecimal weeksTotal   = billRepository.sumNetAmountByBillingDateBetween(weekStart, todayEnd);
        BigDecimal monthsTotal  = billRepository.sumNetAmountByBillingDateBetween(monthStart, todayEnd);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("todays_total",      todaysTotal  != null ? todaysTotal  : BigDecimal.ZERO);
        summary.put("this_weeks_total",  weeksTotal   != null ? weeksTotal   : BigDecimal.ZERO);
        summary.put("this_months_total", monthsTotal  != null ? monthsTotal  : BigDecimal.ZERO);
        return summary;
    }

    @GetMapping("/alerts")
    public Map<String, Object> getAlerts() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().atTime(23, 59, 59);

        List<Map<String, Object>> alerts = new ArrayList<>();

        long lowStock = stockRepository.countLowStockItems();
        if (lowStock > 0) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", "low-stock"); a.put("severity", "critical");
            a.put("title", "Low Stock Alert");
            a.put("desc", lowStock + " medicine(s) are below reorder level.");
            a.put("time", "Live"); alerts.add(a);
        }
        long expiring = stockRepository.countExpiringWithinDays(30);
        if (expiring > 0) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", "expiry"); a.put("severity", "warning");
            a.put("title", "Near Expiry");
            a.put("desc", expiring + " batch(es) expiring within 30 days.");
            a.put("time", "Live"); alerts.add(a);
        }
        long openCredit = creditBillRepository.countByStatus(PaymentStatus.UNPAID);
        if (openCredit > 0) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", "credit"); a.put("severity", "info");
            a.put("title", "Open Credit Bills");
            a.put("desc", openCredit + " unpaid credit bill(s) pending collection.");
            a.put("time", "Live"); alerts.add(a);
        }
        long pendingRx = prescriptionRepository.countByStatus(PrescriptionStatus.PENDING.name());
        if (pendingRx > 0) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", "rx"); a.put("severity", "info");
            a.put("title", "Pending Prescriptions");
            a.put("desc", pendingRx + " prescription(s) awaiting dispensing.");
            a.put("time", "Live"); alerts.add(a);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("alerts", alerts);
        result.put("count",  alerts.size());
        return result;
    }

    @GetMapping("/returns/badge-count")
    public Map<String, Integer> getReturnsBadgeCount() {
        long count = returnRepository.countByStatus(ReturnStatus.PENDING);
        return Map.of("count", (int) count);
    }

    @GetMapping("/pending-prescriptions/badge-count")
    public Map<String, Integer> getPendingPrescriptionsBadgeCount() {
        long count = prescriptionRepository.countByStatus(PrescriptionStatus.PENDING.name());
        return Map.of("count", (int) count);
    }

    @GetMapping("/sales/credit-bills/badge-count")
    public Map<String, Integer> getCreditBillsBadgeCount() {
        long count = creditBillRepository.countByStatus(PaymentStatus.UNPAID);
        return Map.of("count", (int) count);
    }
}
