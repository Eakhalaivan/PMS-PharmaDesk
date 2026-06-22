package com.pharmadesk.backend.pharmacy.dto.analytics;

import java.math.BigDecimal;

public class KPIDTO {
    private BigDecimal currentValue;
    private BigDecimal previousValue;
    private BigDecimal percentageChange;
    private boolean isPositive;

    public KPIDTO() {}

    public KPIDTO(BigDecimal currentValue, BigDecimal previousValue, BigDecimal percentageChange, boolean isPositive) {
        this.currentValue = currentValue;
        this.previousValue = previousValue;
        this.percentageChange = percentageChange;
        this.isPositive = isPositive;
    }

    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }

    public BigDecimal getPreviousValue() { return previousValue; }
    public void setPreviousValue(BigDecimal previousValue) { this.previousValue = previousValue; }

    public BigDecimal getPercentageChange() { return percentageChange; }
    public void setPercentageChange(BigDecimal percentageChange) { this.percentageChange = percentageChange; }

    public boolean isPositive() { return isPositive; }
    public void setPositive(boolean positive) { isPositive = positive; }
}
