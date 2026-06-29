package com.pharmadesk.backend.pharmacy.dto.reports;

public record ExpiryReportDTO(
    String medicine, String batch, java.time.LocalDate expiry, Integer quantity, String supplier, int daysLeft, String urgency
) {}
