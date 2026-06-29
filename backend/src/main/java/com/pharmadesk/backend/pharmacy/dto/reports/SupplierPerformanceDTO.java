package com.pharmadesk.backend.pharmacy.dto.reports;

public record SupplierPerformanceDTO(
    String supplier, Double overallScore, Double onTimeDelivery, Double orderFillRate, Double qualityRejection, Double invoiceAccuracy, java.time.LocalDate periodStart, java.time.LocalDate periodEnd
) {}
