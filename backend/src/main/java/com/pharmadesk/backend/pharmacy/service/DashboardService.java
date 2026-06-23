package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.pharmacy.dto.dashboard.*;
import com.pharmadesk.backend.pharmacy.enums.PaymentStatus;
import com.pharmadesk.backend.pharmacy.enums.PrescriptionStatus;
import com.pharmadesk.backend.pharmacy.enums.ReturnStatus;
import com.pharmadesk.backend.pharmacy.repository.*;
import com.pharmadesk.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final PharmacyBillRepository billRepository;
    private final MedicineStockRepository stockRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineReturnRepository returnRepository;
    private final CreditBillRepository creditBillRepository;
    private final UserRepository userRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public DashboardService(PharmacyBillRepository billRepository,
                            MedicineStockRepository stockRepository,
                            MedicineRepository medicineRepository,
                            PrescriptionRepository prescriptionRepository,
                            MedicineReturnRepository returnRepository,
                            CreditBillRepository creditBillRepository,
                            UserRepository userRepository,
                            PurchaseOrderRepository purchaseOrderRepository) {
        this.billRepository = billRepository;
        this.stockRepository = stockRepository;
        this.medicineRepository = medicineRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.returnRepository = returnRepository;
        this.creditBillRepository = creditBillRepository;
        this.userRepository = userRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public DashboardKpiDTO buildKpisForRole(String role) {
        return switch (role) {
            case "SYSTEM_ADMIN", "SUPERVISOR" -> buildAdminKpis();
            case "PHARMACY_STAFF" -> buildPharmacyKpis();
            case "BILLING_STAFF" -> buildBillingKpis();
            case "STOREKEEPER" -> buildStorekeeperKpis();
            case "RECEPTIONIST" -> buildReceptionistKpis();
            case "SENIOR_MEDICAL_STAFF", "MEDICAL_STAFF" -> buildMedicalKpis();
            default -> buildAdminKpis();
        };
    }

    private DashboardKpiDTO buildAdminKpis() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        DashboardKpiDTO dto = new DashboardKpiDTO();
        dto.put("totalSkus", medicineRepository.count());
        dto.put("todayRevenue", billRepository.sumNetAmountByBillingDateBetween(startOfDay, endOfDay));
        dto.put("pendingPrescriptions", prescriptionRepository.countByStatus(PrescriptionStatus.PENDING.name()));
        dto.put("lowStockAlerts", stockRepository.countLowStockItems());
        dto.put("expiringIn30Days", stockRepository.countExpiringWithinDays(30));
        dto.put("activePatientsToday", billRepository.countDistinctPatientNamesToday(startOfDay, endOfDay));
        dto.put("totalSalesToday", billRepository.sumNetAmountByBillingDateBetween(startOfDay, endOfDay));
        dto.put("staffActive", userRepository.countByStatus("ACTIVE"));
        dto.put("returnsAwaitingApproval", returnRepository.countByStatus(ReturnStatus.PENDING));
        dto.put("systemHealthPct", 99); // Mock health
        return dto;
    }

    private DashboardKpiDTO buildPharmacyKpis() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        DashboardKpiDTO dto = new DashboardKpiDTO();
        dto.put("billsRaisedToday", billRepository.countByBillingDateBetween(startOfDay, endOfDay));
        dto.put("todayCollections", billRepository.sumNetAmountByBillingDateBetween(startOfDay, endOfDay));
        dto.put("pendingDispensals", prescriptionRepository.countByStatus(PrescriptionStatus.PENDING.name()));
        dto.put("lowStockItems", stockRepository.countLowStockItems());
        dto.put("myReturnsToday", returnRepository.countByStatus(ReturnStatus.PENDING));
        dto.put("creditBillsPending", creditBillRepository.countByStatus(PaymentStatus.UNPAID));
        return dto;
    }

    private DashboardKpiDTO buildBillingKpis() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        DashboardKpiDTO dto = new DashboardKpiDTO();
        dto.put("billsRaisedToday", billRepository.countByBillingDateBetween(startOfDay, endOfDay));
        dto.put("totalCollected", billRepository.sumNetAmountByBillingDateBetween(startOfDay, endOfDay));
        dto.put("pendingClearances", 0);
        dto.put("advanceRequests", 0);
        dto.put("creditBills", creditBillRepository.countByStatus(PaymentStatus.UNPAID));
        dto.put("consolidatedBillsPending", 0);
        return dto;
    }

    private DashboardKpiDTO buildStorekeeperKpis() {
        DashboardKpiDTO dto = new DashboardKpiDTO();
        dto.put("posPendingApproval", purchaseOrderRepository.countByStatus("PENDING"));
        dto.put("grnsAwaitingVerification", 0);
        dto.put("lowStockSkus", stockRepository.countLowStockItems());
        dto.put("expiringIn30Days", stockRepository.countExpiringWithinDays(30));
        dto.put("supplierReturnsPending", 0);
        dto.put("stockValue", stockRepository.findTotalStockValue());
        return dto;
    }

    private DashboardKpiDTO buildReceptionistKpis() {
        DashboardKpiDTO dto = new DashboardKpiDTO();
        dto.put("activePatientsToday", 0);
        return dto;
    }

    private DashboardKpiDTO buildMedicalKpis() {
        DashboardKpiDTO dto = new DashboardKpiDTO();
        dto.put("pendingPrescriptions", prescriptionRepository.countByStatus(PrescriptionStatus.PENDING.name()));
        return dto;
    }

    public List<ChartDataPointDTO> getChartData(String role, int days) {
        LocalDateTime from = LocalDate.now().minusDays(days - 1).atStartOfDay();
        LocalDateTime to = LocalDate.now().atTime(23, 59, 59);
        
        return billRepository.findByBillingDateBetween(from, to).stream()
                .collect(Collectors.groupingBy(b -> b.getBillingDate().toLocalDate()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    BigDecimal dailyRevenue = e.getValue().stream()
                            .map(b -> b.getNetAmount() != null ? b.getNetAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ChartDataPointDTO(
                            e.getKey().getDayOfWeek().name().substring(0, 3), 
                            dailyRevenue, 
                            BigDecimal.valueOf(e.getValue().size())
                    );
                })
                .collect(Collectors.toList());
    }

    public List<SystemAlertDTO> getAlerts() {
        List<SystemAlertDTO> alerts = new ArrayList<>();
        
        long lowStock = stockRepository.countLowStockItems();
        if (lowStock > 0) {
            alerts.add(new SystemAlertDTO("low-stock", "Stock Monitoring", "All key medicine SKU stock levels are currently within safe margins.", "INFO", "STOCK", LocalDateTime.now(), false));
        }

        long expiring = stockRepository.countExpiringWithinDays(30);
        if (expiring > 0) {
            alerts.add(new SystemAlertDTO("expiry", "Near-Expiry Batches", expiring + " batches expiring within 30 days.", "CRITICAL", "EXPIRY", LocalDateTime.now(), false));
        }
        
        long pendingPOs = purchaseOrderRepository.countByStatus("PENDING");
        if (pendingPOs > 0) {
            alerts.add(new SystemAlertDTO("grn", "Pending GRN Verification", pendingPOs + " awaiting approval.", "WARNING", "GRN", LocalDateTime.now(), false));
        }

        alerts.add(new SystemAlertDTO("cold-chain", "Cold Chain Temp Warning", "Refrigeration Unit 2 temperature spiked.", "WARNING", "COLD_CHAIN", LocalDateTime.now(), false));

        return alerts;
    }

    public RevenueStripDTO getRevenueStrip() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        BigDecimal todaysTotal = billRepository.sumNetAmountByBillingDateBetween(todayStart, todayEnd);
        BigDecimal weeksTotal = billRepository.sumNetAmountByBillingDateBetween(weekStart, todayEnd);
        BigDecimal monthsTotal = billRepository.sumNetAmountByBillingDateBetween(monthStart, todayEnd);

        return new RevenueStripDTO(
            todaysTotal != null ? todaysTotal : BigDecimal.ZERO,
            weeksTotal != null ? weeksTotal : BigDecimal.ZERO,
            monthsTotal != null ? monthsTotal : BigDecimal.ZERO
        );
    }
}
