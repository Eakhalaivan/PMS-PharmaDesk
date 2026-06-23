package com.pharmadesk.backend.pharmacy.dto.dashboard;

import java.math.BigDecimal;

public class ChartDataPointDTO {
    private String label;
    private BigDecimal value;
    private BigDecimal secondary;

    public ChartDataPointDTO() {}

    public ChartDataPointDTO(String label, BigDecimal value) {
        this.label = label;
        this.value = value;
    }

    public ChartDataPointDTO(String label, BigDecimal value, BigDecimal secondary) {
        this.label = label;
        this.value = value;
        this.secondary = secondary;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getSecondary() { return secondary; }
    public void setSecondary(BigDecimal secondary) { this.secondary = secondary; }
}
