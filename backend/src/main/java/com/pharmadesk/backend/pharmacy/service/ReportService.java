package com.pharmadesk.backend.pharmacy.service;

import com.pharmadesk.backend.model.MedicineStock;
import com.pharmadesk.backend.model.PharmacyBill;
import com.pharmadesk.backend.pharmacy.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final PharmacyBillRepository billRepository;
    private final MedicineStockRepository stockRepository;
    private final GoodsReceiptNoteRepository grnRepository;
    private final SupplierInvoiceRepository invoiceRepository;
    private final PurchaseOrderRepository poRepository;
    private final SupplierPerformanceRepository performanceRepository;

    public ReportService(PharmacyBillRepository billRepository,
                         MedicineStockRepository stockRepository,
                         GoodsReceiptNoteRepository grnRepository,
                         SupplierInvoiceRepository invoiceRepository,
                         PurchaseOrderRepository poRepository,
                         SupplierPerformanceRepository performanceRepository) {
        this.billRepository = billRepository;
        this.stockRepository = stockRepository;
        this.grnRepository = grnRepository;
        this.invoiceRepository = invoiceRepository;
        this.poRepository = poRepository;
        this.performanceRepository = performanceRepository;
    }

    // ─── SALES REPORTS ─────────────────────────────────────────

    public List<Map<String, Object>> getSalesReport(LocalDateTime from, LocalDateTime to) {
        return billRepository.findByBillingDateBetween(from, to).stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("billNumber", b.getBillNumber());
            m.put("date", b.getBillingDate());
            m.put("patient", b.getPatientName());
            m.put("doctorName", b.getDoctorName());
            m.put("billType", b.getBillType());
            m.put("paymentMode", b.getPaymentMode());
            m.put("subTotal", b.getSubTotal());
            m.put("discount", b.getDiscountAmount());
            m.put("tax", b.getTaxAmount());
            m.put("amount", b.getNetAmount());
            m.put("status", b.getStatus());
            return m;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getDailySalesSummary(LocalDateTime from, LocalDateTime to) {
        List<PharmacyBill> bills = billRepository.findByBillingDateBetween(from, to);
        BigDecimal totalRevenue = bills.stream().map(b -> nvl(b.getNetAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTax    = bills.stream().map(b -> nvl(b.getTaxAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDisc   = bills.stream().map(b -> nvl(b.getDiscountAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        long cashBills   = bills.stream().filter(b -> "CASH".equals(b.getBillType()) || "OTC".equals(b.getBillType())).count();
        long creditBills = bills.stream().filter(b -> "CREDIT".equals(b.getBillType())).count();

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalRevenue", totalRevenue);
        m.put("totalTax", totalTax);
        m.put("totalDiscount", totalDisc);
        m.put("netRevenue", totalRevenue.subtract(totalTax));
        m.put("billCount", bills.size());
        m.put("cashBills", cashBills);
        m.put("creditBills", creditBills);
        m.put("period", from.toLocalDate() + " to " + to.toLocalDate());
        return m;
    }

    public List<Map<String, Object>> getMedicineWiseSales(LocalDateTime from, LocalDateTime to) {
        List<PharmacyBill> bills = billRepository.findByBillingDateBetweenWithItems(from, to);
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        bills.forEach(b -> b.getItems().forEach(item -> {
            String name = item.getStock() != null && item.getStock().getMedicine() != null
                    ? item.getStock().getMedicine().getName() : "Unknown";
            grouped.computeIfAbsent(name, k -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("medicine", k);
                entry.put("unitsSold", 0);
                entry.put("revenue", BigDecimal.ZERO);
                entry.put("tax", BigDecimal.ZERO);
                return entry;
            });
            Map<String, Object> entry = grouped.get(name);
            entry.put("unitsSold", (int) entry.get("unitsSold") + nvlInt(item.getQuantity()));
            entry.put("revenue", ((BigDecimal) entry.get("revenue")).add(nvl(item.getNetAmount())));
            entry.put("tax", ((BigDecimal) entry.get("tax")).add(nvl(item.getTaxAmount())));
        }));
        return new ArrayList<>(grouped.values());
    }

    public List<Map<String, Object>> getItemisedSalesRegister(LocalDateTime from, LocalDateTime to) {
        List<PharmacyBill> bills = billRepository.findByBillingDateBetweenWithItems(from, to);
        List<Map<String, Object>> rows = new ArrayList<>();
        bills.forEach(b -> b.getItems().forEach(item -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("billNumber", b.getBillNumber());
            m.put("date", b.getBillingDate());
            m.put("patient", b.getPatientName());
            m.put("doctor", b.getDoctorName());
            String med = item.getStock() != null && item.getStock().getMedicine() != null
                    ? item.getStock().getMedicine().getName() : "—";
            m.put("medicine", med);
            String hsn = item.getStock() != null && item.getStock().getMedicine() != null
                    ? item.getStock().getMedicine().getHsnCode() : "—";
            m.put("hsnCode", hsn);
            m.put("quantity", item.getQuantity());
            m.put("unitPrice", item.getUnitPrice());
            m.put("discount", item.getDiscountAmount());
            m.put("tax", item.getTaxAmount());
            m.put("netAmount", item.getNetAmount());
            rows.add(m);
        }));
        return rows;
    }

    public List<Map<String, Object>> getCreditSalesReport(LocalDateTime from, LocalDateTime to) {
        List<PharmacyBill> bills = billRepository.findByBillingDateBetween(from, to)
                .stream().filter(b -> "CREDIT".equals(b.getBillType())).collect(Collectors.toList());
        return bills.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("billNumber", b.getBillNumber());
            m.put("date", b.getBillingDate());
            m.put("patient", b.getPatientName());
            m.put("netAmount", b.getNetAmount());
            m.put("paidAmount", b.getPaidAmount());
            m.put("balanceAmount", b.getBalanceAmount());
            m.put("status", b.getStatus());
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getCancelledBillsReport(LocalDateTime from, LocalDateTime to) {
        return billRepository.findByBillingDateBetween(from, to).stream()
                .filter(b -> "CANCELLED".equals(b.getStatus()))
                .map(b -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("billNumber", b.getBillNumber());
                    m.put("date", b.getBillingDate());
                    m.put("patient", b.getPatientName());
                    m.put("amount", b.getNetAmount());
                    m.put("cancelledBy", b.getCreatedBy());
                    return m;
                }).collect(Collectors.toList());
    }

    // ─── GST REPORTS ─────────────────────────────────────────────

    public Map<String, Object> getTaxReport(LocalDateTime from, LocalDateTime to) {
        List<PharmacyBill> bills = billRepository.findByBillingDateBetween(from, to);
        BigDecimal totalTax    = bills.stream().map(b -> nvl(b.getTaxAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = bills.stream().map(b -> nvl(b.getNetAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cgst = totalTax.divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal sgst = totalTax.subtract(cgst);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalTax", totalTax);
        report.put("cgst", cgst);
        report.put("sgst", sgst);
        report.put("igst", BigDecimal.ZERO);
        report.put("totalAmount", totalAmount);
        report.put("taxableAmount", totalAmount.subtract(totalTax));
        report.put("billCount", bills.size());
        report.put("period", from.toLocalDate() + " to " + to.toLocalDate());
        return report;
    }

    public List<Map<String, Object>> getGstSalesRegister(LocalDateTime from, LocalDateTime to) {
        return getItemisedSalesRegister(from, to).stream().map(row -> {
            BigDecimal net = row.get("netAmount") != null ? (BigDecimal) row.get("netAmount") : BigDecimal.ZERO;
            BigDecimal tax = row.get("tax") != null ? (BigDecimal) row.get("tax") : BigDecimal.ZERO;
            BigDecimal taxable = net.subtract(tax);
            BigDecimal cgst = tax.divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
            row.put("taxableValue", taxable);
            row.put("cgst", cgst);
            row.put("sgst", tax.subtract(cgst));
            row.put("igst", BigDecimal.ZERO);
            row.put("totalGst", tax);
            return row;
        }).collect(Collectors.toList());
    }

    // ─── STOCK REPORTS ─────────────────────────────────────────────

    public List<Map<String, Object>> getStockReport() {
        return stockRepository.findAll().stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("medicine", s.getMedicine() != null ? s.getMedicine().getName() : "Unknown");
            m.put("category", s.getMedicine() != null ? s.getMedicine().getCategory() : "—");
            m.put("hsnCode", s.getMedicine() != null ? s.getMedicine().getHsnCode() : "—");
            m.put("batch", s.getBatchNumber());
            m.put("quantity", s.getQuantityAvailable());
            m.put("unitPrice", s.getPurchaseRate());
            m.put("mrp", s.getSellingRate());
            m.put("expiry", s.getExpiryDate());
            m.put("supplier", s.getSupplier() != null ? s.getSupplier().getName() : "—");
            m.put("value", s.getPurchaseRate() != null ? s.getPurchaseRate().multiply(BigDecimal.valueOf(nvlInt(s.getQuantityAvailable()))) : BigDecimal.ZERO);
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getExpiryReport(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        LocalDate today = LocalDate.now();
        return stockRepository.findByExpiryDateBefore(threshold).stream()
                .filter(s -> s.getQuantityAvailable() != null && s.getQuantityAvailable() > 0)
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("medicine", s.getMedicine() != null ? s.getMedicine().getName() : "Unknown");
                    m.put("batch", s.getBatchNumber());
                    m.put("expiry", s.getExpiryDate());
                    m.put("quantity", s.getQuantityAvailable());
                    m.put("supplier", s.getSupplier() != null ? s.getSupplier().getName() : "—");
                    int daysLeft = s.getExpiryDate() != null ? (int) (s.getExpiryDate().toEpochDay() - today.toEpochDay()) : 0;
                    m.put("daysLeft", daysLeft);
                    m.put("urgency", daysLeft <= 0 ? "EXPIRED" : daysLeft <= 15 ? "CRITICAL" : daysLeft <= 30 ? "WARNING" : "EARLY_ALERT");
                    return m;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSlowMovingStockReport(LocalDateTime from, LocalDateTime to, int threshold) {
        List<Map<String, Object>> salesData = getMedicineWiseSales(from, to);
        Set<String> movingMeds = salesData.stream()
                .filter(r -> (int) r.get("unitsSold") >= threshold)
                .map(r -> (String) r.get("medicine"))
                .collect(Collectors.toSet());

        return stockRepository.findAll().stream()
                .filter(s -> s.getQuantityAvailable() != null && s.getQuantityAvailable() > 0)
                .map(s -> s.getMedicine() != null ? s.getMedicine().getName() : "Unknown")
                .filter(name -> !movingMeds.contains(name))
                .distinct()
                .map(name -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("medicine", name);
                    m.put("soldInPeriod", salesData.stream()
                            .filter(r -> name.equals(r.get("medicine")))
                            .mapToInt(r -> (int) r.get("unitsSold")).sum());
                    return m;
                }).collect(Collectors.toList());
    }

    // ─── PURCHASE REPORTS ─────────────────────────────────────────

    public List<Map<String, Object>> getPurchaseRegister(LocalDateTime from, LocalDateTime to) {
        return grnRepository.findAll().stream()
                .filter(g -> g.getReceivedDate() != null && !g.getReceivedDate().isBefore(from) && !g.getReceivedDate().isAfter(to))
                .map(g -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("grnNumber", g.getGrnNumber());
                    m.put("date", g.getReceivedDate());
                    m.put("supplier", g.getSupplier() != null ? g.getSupplier().getName() : "—");
                    m.put("invoiceNumber", g.getSupplierInvoiceNumber());
                    m.put("status", g.getStatus());
                    m.put("itemCount", g.getItems() != null ? g.getItems().size() : 0);
                    return m;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getOutstandingPayables() {
        LocalDate today = LocalDate.now();
        return invoiceRepository.findAll().stream()
                .filter(inv -> !"PAID".equals(inv.getStatus()))
                .map(inv -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("invoiceNumber", inv.getInvoiceNumber());
                    m.put("supplier", inv.getSupplier() != null ? inv.getSupplier().getName() : "—");
                    m.put("totalAmount", inv.getTotalAmount());
                    m.put("status", inv.getStatus());
                    int daysOld = inv.getInvoiceDate() != null ? (int) (today.toEpochDay() - inv.getInvoiceDate().toEpochDay()) : 0;
                    m.put("daysOld", daysOld);
                    m.put("agingBucket", daysOld <= 30 ? "0-30 days" : daysOld <= 60 ? "31-60 days" : "61+ days");
                    return m;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSupplierPerformanceSummary() {
        return performanceRepository.findAll().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("supplier", p.getSupplier() != null ? p.getSupplier().getName() : "—");
            m.put("overallScore", p.getOverallScore());
            m.put("onTimeDelivery", p.getOnTimeDeliveryRate());
            m.put("orderFillRate", p.getOrderFillRate());
            m.put("qualityRejection", p.getQualityRejectionRate());
            m.put("invoiceAccuracy", p.getInvoiceAccuracyRate());
            m.put("periodStart", p.getPeriodStart());
            m.put("periodEnd", p.getPeriodEnd());
            return m;
        }).sorted((a, b) -> {
            double scoreA = ((Number) a.getOrDefault("overallScore", 0)).doubleValue();
            double scoreB = ((Number) b.getOrDefault("overallScore", 0)).doubleValue();
            return Double.compare(scoreB, scoreA);
        }).collect(Collectors.toList());
    }


    // ─── Helpers ───────────────────────────────────────────────
    private BigDecimal nvl(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
    private int nvlInt(Integer v) { return v != null ? v : 0; }
}
