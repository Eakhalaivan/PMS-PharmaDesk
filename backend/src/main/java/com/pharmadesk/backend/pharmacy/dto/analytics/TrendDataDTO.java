package com.pharmadesk.backend.pharmacy.dto.analytics;

import java.math.BigDecimal;

public class TrendDataDTO {
    private String dateLabel;
    private BigDecimal revenue;
    private Integer unitsDispensed;
    private BigDecimal returnsValue;

    public TrendDataDTO() {}

    public TrendDataDTO(String dateLabel, BigDecimal revenue, Integer unitsDispensed) {
        this.dateLabel = dateLabel;
        this.revenue = revenue;
        this.unitsDispensed = unitsDispensed;
    }

    public String getDateLabel() { return dateLabel; }
    public void setDateLabel(String dateLabel) { this.dateLabel = dateLabel; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

    public Integer getUnitsDispensed() { return unitsDispensed; }
    public void setUnitsDispensed(Integer unitsDispensed) { this.unitsDispensed = unitsDispensed; }

    public BigDecimal getReturnsValue() { return returnsValue; }
    public void setReturnsValue(BigDecimal returnsValue) { this.returnsValue = returnsValue; }
}
