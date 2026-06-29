package com.pharmadesk.backend.pharmacy.dto.reports;

public record TaxReportDTO(
    java.math.BigDecimal totalTax, java.math.BigDecimal cgst, java.math.BigDecimal sgst, java.math.BigDecimal igst, java.math.BigDecimal totalAmount, java.math.BigDecimal taxableAmount, int billCount, String period
) {}
