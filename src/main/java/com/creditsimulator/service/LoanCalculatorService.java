package com.creditsimulator.service;

import com.creditsimulator.domain.CalculationResult;
import com.creditsimulator.domain.LoanRequest;
import com.creditsimulator.domain.YearlyInstallment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Declining-balance, recalculated-yearly amortization, matching Rumus.xlsx:
 * each year the remaining principal accrues that year's interest rate,
 * and is divided across the remaining months of the tenor.
 */
public class LoanCalculatorService {

    private final InterestRateService interestRateService;

    public LoanCalculatorService(InterestRateService interestRateService) {
        this.interestRateService = interestRateService;
    }

    public CalculationResult calculate(LoanRequest request) {
        BigDecimal principal = request.getTotalLoanAmount().subtract(request.getDownPayment());
        int tenorYears = request.getTenorYears();

        List<BigDecimal> rates = interestRateService.ratesForTenor(
                request.getVehicle().getBaseInterestRate(), tenorYears);

        List<YearlyInstallment> breakdown = new ArrayList<>();
        BigDecimal remainingPrincipal = principal;

        for (int year = 1; year <= tenorYears; year++) {
            BigDecimal rate = rates.get(year - 1);
            BigDecimal totalPinjamanTahunIni = remainingPrincipal.add(remainingPrincipal.multiply(rate));

            int sisaBulan = (tenorYears * 12) - ((year - 1) * 12);
            BigDecimal installmentMonthly = totalPinjamanTahunIni
                    .divide(BigDecimal.valueOf(sisaBulan), 2, RoundingMode.HALF_UP);
            BigDecimal installmentYearly = installmentMonthly.multiply(BigDecimal.valueOf(12));

            breakdown.add(new YearlyInstallment(year, rate, totalPinjamanTahunIni, installmentMonthly, installmentYearly));

            remainingPrincipal = totalPinjamanTahunIni.subtract(installmentYearly);
        }

        return new CalculationResult(principal, breakdown);
    }
}
