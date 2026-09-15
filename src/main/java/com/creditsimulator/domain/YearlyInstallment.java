package com.creditsimulator.domain;

import java.math.BigDecimal;

public class YearlyInstallment {

    private final int year;
    private final BigDecimal rate;
    private final BigDecimal totalPinjamanTahunIni;
    private final BigDecimal installmentMonthly;
    private final BigDecimal installmentYearly;

    public YearlyInstallment(int year, BigDecimal rate, BigDecimal totalPinjamanTahunIni,
                              BigDecimal installmentMonthly, BigDecimal installmentYearly) {
        this.year = year;
        this.rate = rate;
        this.totalPinjamanTahunIni = totalPinjamanTahunIni;
        this.installmentMonthly = installmentMonthly;
        this.installmentYearly = installmentYearly;
    }

    public int getYear() {
        return year;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getTotalPinjamanTahunIni() {
        return totalPinjamanTahunIni;
    }

    public BigDecimal getInstallmentMonthly() {
        return installmentMonthly;
    }

    public BigDecimal getInstallmentYearly() {
        return installmentYearly;
    }
}
