package com.pharmadesk.backend.pharmacy.dto.reports;

public record CreditSaleDTO(
    String billNumber, java.time.LocalDateTime date, String patient, java.math.BigDecimal netAmount, java.math.BigDecimal paidAmount, java.math.BigDecimal balanceAmount, String status
) {}
