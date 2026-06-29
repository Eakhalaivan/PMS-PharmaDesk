import os

dto_dir = "backend/src/main/java/com/pharmadesk/backend/pharmacy/dto/reports"
os.makedirs(dto_dir, exist_ok=True)

dtos = {
    "SalesReportRowDTO": "String billNumber, java.time.LocalDateTime date, String patient, String doctorName, String billType, String paymentMode, java.math.BigDecimal subTotal, java.math.BigDecimal discount, java.math.BigDecimal tax, java.math.BigDecimal amount, String status",
    "DailySalesSummaryDTO": "java.math.BigDecimal totalRevenue, java.math.BigDecimal totalTax, java.math.BigDecimal totalDiscount, java.math.BigDecimal netRevenue, long billCount, long cashBills, long creditBills, String period",
    "MedicineWiseSaleDTO": "String medicine, Integer unitsSold, java.math.BigDecimal revenue, java.math.BigDecimal tax",
    "ItemisedSaleDTO": "String billNumber, java.time.LocalDateTime date, String patient, String doctor, String medicine, String hsnCode, Integer quantity, java.math.BigDecimal unitPrice, java.math.BigDecimal discount, java.math.BigDecimal tax, java.math.BigDecimal netAmount",
    "CreditSaleDTO": "String billNumber, java.time.LocalDateTime date, String patient, java.math.BigDecimal netAmount, java.math.BigDecimal paidAmount, java.math.BigDecimal balanceAmount, com.pharmadesk.backend.pharmacy.enums.PaymentStatus status",
    "CancelledBillDTO": "String billNumber, java.time.LocalDateTime date, String patient, java.math.BigDecimal amount, String cancelledBy",
    "TaxReportDTO": "java.math.BigDecimal totalTax, java.math.BigDecimal cgst, java.math.BigDecimal sgst, java.math.BigDecimal igst, java.math.BigDecimal totalAmount, java.math.BigDecimal taxableAmount, int billCount, String period",
    "GstSaleRegisterDTO": "String billNumber, java.time.LocalDateTime date, String patient, String doctor, String medicine, String hsnCode, Integer quantity, java.math.BigDecimal unitPrice, java.math.BigDecimal discount, java.math.BigDecimal tax, java.math.BigDecimal netAmount, java.math.BigDecimal taxableValue, java.math.BigDecimal cgst, java.math.BigDecimal sgst, java.math.BigDecimal igst, java.math.BigDecimal totalGst",
    "StockReportDTO": "String medicine, String category, String hsnCode, String batch, Integer quantity, java.math.BigDecimal unitPrice, java.math.BigDecimal mrp, java.time.LocalDate expiry, String supplier, java.math.BigDecimal value",
    "ExpiryReportDTO": "String medicine, String batch, java.time.LocalDate expiry, Integer quantity, String supplier, int daysLeft, String urgency",
    "SlowMovingStockDTO": "String medicine, int soldInPeriod",
    "PurchaseRegisterDTO": "String grnNumber, java.time.LocalDate date, String supplier, String invoiceNumber, String status, int itemCount",
    "OutstandingPayableDTO": "String invoiceNumber, String supplier, java.math.BigDecimal totalAmount, String status, int daysOld, String agingBucket",
    "SupplierPerformanceDTO": "String supplier, Double overallScore, Double onTimeDelivery, Double orderFillRate, Double qualityRejection, Double invoiceAccuracy, java.time.LocalDate periodStart, java.time.LocalDate periodEnd"
}

for name, fields in dtos.items():
    content = f"""package com.pharmadesk.backend.pharmacy.dto.reports;

public record {name}(
    {fields}
) {{}}
"""
    with open(f"{dto_dir}/{name}.java", 'w') as f:
        f.write(content)

print("DTOs created.")
