package com.pharmadesk.backend.pharmacy.dto.reports;

public record StockReportDTO(
    String medicine, String category, String hsnCode, String batch, Integer quantity, java.math.BigDecimal unitPrice, java.math.BigDecimal mrp, java.time.LocalDate expiry, String supplier, java.math.BigDecimal value
) {}
