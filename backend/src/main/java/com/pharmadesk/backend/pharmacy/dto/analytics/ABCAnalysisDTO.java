package com.pharmadesk.backend.pharmacy.dto.analytics;

import java.math.BigDecimal;

public class ABCAnalysisDTO {
    private Long medicineId;
    private String medicineName;
    private String category; // A, B, C
    private BigDecimal revenueContribution;
    private BigDecimal percentageOfTotal;
    private BigDecimal cumulativePercentage;
    private Integer unitsDispensed;
    private BigDecimal currentStockValue;

    // Getters and Setters
    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getRevenueContribution() { return revenueContribution; }
    public void setRevenueContribution(BigDecimal revenueContribution) { this.revenueContribution = revenueContribution; }

    public BigDecimal getPercentageOfTotal() { return percentageOfTotal; }
    public void setPercentageOfTotal(BigDecimal percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }

    public BigDecimal getCumulativePercentage() { return cumulativePercentage; }
    public void setCumulativePercentage(BigDecimal cumulativePercentage) { this.cumulativePercentage = cumulativePercentage; }

    public Integer getUnitsDispensed() { return unitsDispensed; }
    public void setUnitsDispensed(Integer unitsDispensed) { this.unitsDispensed = unitsDispensed; }

    public BigDecimal getCurrentStockValue() { return currentStockValue; }
    public void setCurrentStockValue(BigDecimal currentStockValue) { this.currentStockValue = currentStockValue; }
}
