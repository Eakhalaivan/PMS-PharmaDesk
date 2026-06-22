package com.pharmadesk.backend.pharmacy.dto.analytics;

import java.math.BigDecimal;
import java.util.List;

public class MonthOverMonthDTO {
    private MonthData monthA;
    private MonthData monthB;
    
    private BigDecimal revenueDifference;
    private BigDecimal revenuePercentageChange;
    
    private List<MedicineStatsDTO> newMedicinesInMonthB;
    private List<MedicineStatsDTO> droppedMedicinesInMonthB;
    
    private List<TrendDataDTO> sixMonthTrend;

    public static class MonthData {
        private String monthName;
        private BigDecimal totalRevenue;
        private Integer totalUnitsDispensed;
        private Integer totalTransactions;
        private BigDecimal averageTransactionValue;
        private BigDecimal returnRatePercentage;
        private BigDecimal creditSalesPercentage;
        private List<MedicineStatsDTO> top10Medicines;

        // Getters and Setters
        public String getMonthName() { return monthName; }
        public void setMonthName(String monthName) { this.monthName = monthName; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        public Integer getTotalUnitsDispensed() { return totalUnitsDispensed; }
        public void setTotalUnitsDispensed(Integer totalUnitsDispensed) { this.totalUnitsDispensed = totalUnitsDispensed; }
        public Integer getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }
        public BigDecimal getAverageTransactionValue() { return averageTransactionValue; }
        public void setAverageTransactionValue(BigDecimal averageTransactionValue) { this.averageTransactionValue = averageTransactionValue; }
        public BigDecimal getReturnRatePercentage() { return returnRatePercentage; }
        public void setReturnRatePercentage(BigDecimal returnRatePercentage) { this.returnRatePercentage = returnRatePercentage; }
        public BigDecimal getCreditSalesPercentage() { return creditSalesPercentage; }
        public void setCreditSalesPercentage(BigDecimal creditSalesPercentage) { this.creditSalesPercentage = creditSalesPercentage; }
        public List<MedicineStatsDTO> getTop10Medicines() { return top10Medicines; }
        public void setTop10Medicines(List<MedicineStatsDTO> top10Medicines) { this.top10Medicines = top10Medicines; }
    }

    // Getters and Setters
    public MonthData getMonthA() { return monthA; }
    public void setMonthA(MonthData monthA) { this.monthA = monthA; }
    public MonthData getMonthB() { return monthB; }
    public void setMonthB(MonthData monthB) { this.monthB = monthB; }
    public BigDecimal getRevenueDifference() { return revenueDifference; }
    public void setRevenueDifference(BigDecimal revenueDifference) { this.revenueDifference = revenueDifference; }
    public BigDecimal getRevenuePercentageChange() { return revenuePercentageChange; }
    public void setRevenuePercentageChange(BigDecimal revenuePercentageChange) { this.revenuePercentageChange = revenuePercentageChange; }
    public List<MedicineStatsDTO> getNewMedicinesInMonthB() { return newMedicinesInMonthB; }
    public void setNewMedicinesInMonthB(List<MedicineStatsDTO> newMedicinesInMonthB) { this.newMedicinesInMonthB = newMedicinesInMonthB; }
    public List<MedicineStatsDTO> getDroppedMedicinesInMonthB() { return droppedMedicinesInMonthB; }
    public void setDroppedMedicinesInMonthB(List<MedicineStatsDTO> droppedMedicinesInMonthB) { this.droppedMedicinesInMonthB = droppedMedicinesInMonthB; }
    public List<TrendDataDTO> getSixMonthTrend() { return sixMonthTrend; }
    public void setSixMonthTrend(List<TrendDataDTO> sixMonthTrend) { this.sixMonthTrend = sixMonthTrend; }
}
