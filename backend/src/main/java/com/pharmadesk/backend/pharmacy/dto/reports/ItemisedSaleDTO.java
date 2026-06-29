package com.pharmadesk.backend.pharmacy.dto.reports;

public record ItemisedSaleDTO(
    String billNumber, java.time.LocalDateTime date, String patient, String doctor, String medicine, String hsnCode, Integer quantity, java.math.BigDecimal unitPrice, java.math.BigDecimal discount, java.math.BigDecimal tax, java.math.BigDecimal netAmount
) {}
