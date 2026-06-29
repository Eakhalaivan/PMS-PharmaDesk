package com.pharmadesk.backend.pharmacy.dto.reports;

public record OutstandingPayableDTO(
    String invoiceNumber, String supplier, java.math.BigDecimal totalAmount, String status, int daysOld, String agingBucket
) {}
