package com.pharmadesk.backend.pharmacy.dto.dashboard;

import java.math.BigDecimal;

public class RevenueStripDTO {
    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;
    private String currency;

    public RevenueStripDTO() {
        this.currency = "INR";
    }

    public RevenueStripDTO(BigDecimal todayRevenue, BigDecimal weekRevenue, BigDecimal monthRevenue) {
        this.todayRevenue = todayRevenue;
        this.weekRevenue = weekRevenue;
        this.monthRevenue = monthRevenue;
        this.currency = "INR";
    }

    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }

    public BigDecimal getWeekRevenue() { return weekRevenue; }
    public void setWeekRevenue(BigDecimal weekRevenue) { this.weekRevenue = weekRevenue; }

    public BigDecimal getMonthRevenue() { return monthRevenue; }
    public void setMonthRevenue(BigDecimal monthRevenue) { this.monthRevenue = monthRevenue; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
