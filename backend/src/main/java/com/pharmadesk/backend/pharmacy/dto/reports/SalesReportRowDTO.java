package com.pharmadesk.backend.pharmacy.dto.reports;

public record SalesReportRowDTO(
    String billNumber, java.time.LocalDateTime date, String patient, String doctorName, String billType, String paymentMode, java.math.BigDecimal subTotal, java.math.BigDecimal discount, java.math.BigDecimal tax, java.math.BigDecimal amount, String status
) {}
