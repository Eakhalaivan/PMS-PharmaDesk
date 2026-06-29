package com.pharmadesk.backend.pharmacy.dto.reports;

public record GstSaleRegisterDTO(
    String billNumber, java.time.LocalDateTime date, String patient, String doctor, String medicine, String hsnCode, Integer quantity, java.math.BigDecimal unitPrice, java.math.BigDecimal discount, java.math.BigDecimal tax, java.math.BigDecimal netAmount, java.math.BigDecimal taxableValue, java.math.BigDecimal cgst, java.math.BigDecimal sgst, java.math.BigDecimal igst, java.math.BigDecimal totalGst
) {}
