package com.creditsimulator.domain;

import java.math.BigDecimal;
import java.util.List;

public class CalculationResult {

    private final BigDecimal pokokPinjaman;
    private final List<YearlyInstallment> yearlyBreakdown;

    public CalculationResult(BigDecimal pokokPinjaman, List<YearlyInstallment> yearlyBreakdown) {
        this.pokokPinjaman = pokokPinjaman;
        this.yearlyBreakdown = yearlyBreakdown;
    }

    public BigDecimal getPokokPinjaman() {
        return pokokPinjaman;
    }

    public List<YearlyInstallment> getYearlyBreakdown() {
        return yearlyBreakdown;
    }

    public BigDecimal getFirstYearInstallmentMonthly() {
        return yearlyBreakdown.get(0).getInstallmentMonthly();
    }
}
