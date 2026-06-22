package com.pharmadesk.backend.pharmacy.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MedicineStatsDTO {
    private Long medicineId;
    private String medicineName;
    private String drugClass;
    private Integer totalUnitsDispensed;
    private BigDecimal totalSalesValue;
    private Integer numberOfTransactions;
    private Double averageUnitsPerTransaction;
    private Integer currentStockLevel;
    private Integer daysOfStockRemaining;
    private Boolean reorderRecommendation;
    private LocalDateTime lastDispensedDate;
    private BigDecimal stockValueLocked;
    private LocalDateTime nearestExpiryDate;
    private Integer daysUntilNearestExpiry;

    // Getters and Setters
    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDrugClass() { return drugClass; }
    public void setDrugClass(String drugClass) { this.drugClass = drugClass; }

    public Integer getTotalUnitsDispensed() { return totalUnitsDispensed; }
    public void setTotalUnitsDispensed(Integer totalUnitsDispensed) { this.totalUnitsDispensed = totalUnitsDispensed; }

    public BigDecimal getTotalSalesValue() { return totalSalesValue; }
    public void setTotalSalesValue(BigDecimal totalSalesValue) { this.totalSalesValue = totalSalesValue; }

    public Integer getNumberOfTransactions() { return numberOfTransactions; }
    public void setNumberOfTransactions(Integer numberOfTransactions) { this.numberOfTransactions = numberOfTransactions; }

    public Double getAverageUnitsPerTransaction() { return averageUnitsPerTransaction; }
    public void setAverageUnitsPerTransaction(Double averageUnitsPerTransaction) { this.averageUnitsPerTransaction = averageUnitsPerTransaction; }

    public Integer getCurrentStockLevel() { return currentStockLevel; }
    public void setCurrentStockLevel(Integer currentStockLevel) { this.currentStockLevel = currentStockLevel; }

    public Integer getDaysOfStockRemaining() { return daysOfStockRemaining; }
    public void setDaysOfStockRemaining(Integer daysOfStockRemaining) { this.daysOfStockRemaining = daysOfStockRemaining; }

    public Boolean getReorderRecommendation() { return reorderRecommendation; }
    public void setReorderRecommendation(Boolean reorderRecommendation) { this.reorderRecommendation = reorderRecommendation; }

    public LocalDateTime getLastDispensedDate() { return lastDispensedDate; }
    public void setLastDispensedDate(LocalDateTime lastDispensedDate) { this.lastDispensedDate = lastDispensedDate; }

    public BigDecimal getStockValueLocked() { return stockValueLocked; }
    public void setStockValueLocked(BigDecimal stockValueLocked) { this.stockValueLocked = stockValueLocked; }

    public LocalDateTime getNearestExpiryDate() { return nearestExpiryDate; }
    public void setNearestExpiryDate(LocalDateTime nearestExpiryDate) { this.nearestExpiryDate = nearestExpiryDate; }

    public Integer getDaysUntilNearestExpiry() { return daysUntilNearestExpiry; }
    public void setDaysUntilNearestExpiry(Integer daysUntilNearestExpiry) { this.daysUntilNearestExpiry = daysUntilNearestExpiry; }
}
