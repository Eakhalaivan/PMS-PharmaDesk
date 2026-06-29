package com.pharmadesk.backend.pharmacy.dto.reports;

public record DailySalesSummaryDTO(
    java.math.BigDecimal totalRevenue, java.math.BigDecimal totalTax, java.math.BigDecimal totalDiscount, java.math.BigDecimal netRevenue, long billCount, long cashBills, long creditBills, String period
) {}
