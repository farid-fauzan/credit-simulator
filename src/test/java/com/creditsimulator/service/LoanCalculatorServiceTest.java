package com.creditsimulator.service;

import com.creditsimulator.domain.CalculationResult;
import com.creditsimulator.domain.LoanRequest;
import com.creditsimulator.domain.Mobil;
import com.creditsimulator.domain.VehicleCondition;
import com.creditsimulator.domain.YearlyInstallment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verified against the reference calculation in Rumus.xlsx:
 * Harga Mobil 100,000,000 / DP 25% -> Pokok 75,000,000, tenor 3 tahun.
 */
class LoanCalculatorServiceTest {

    private final LoanCalculatorService service = new LoanCalculatorService(new InterestRateService());

    @Test
    void matchesRumusXlsxReferenceCalculation() {
        LoanRequest request = new LoanRequest(
                new Mobil(),
                VehicleCondition.BEKAS,
                2023,
                new BigDecimal("100000000"),
                3,
                new BigDecimal("25000000")
        );

        CalculationResult result = service.calculate(request);
        List<YearlyInstallment> breakdown = result.getYearlyBreakdown();

        assertNumericallyEquals("75000000", result.getPokokPinjaman());

        assertNumericallyEquals("81000000.00", breakdown.get(0).getTotalPinjamanTahunIni());
        assertNumericallyEquals("2250000.00", breakdown.get(0).getInstallmentMonthly());

        assertNumericallyEquals("58374000.00", breakdown.get(1).getTotalPinjamanTahunIni());
        assertNumericallyEquals("2432250.00", breakdown.get(1).getInstallmentMonthly());

        assertNumericallyEquals("31697082.00", breakdown.get(2).getTotalPinjamanTahunIni());
        assertNumericallyEquals("2641423.50", breakdown.get(2).getInstallmentMonthly());
    }

    private void assertNumericallyEquals(String expected, BigDecimal actual) {
        assertTrue(new BigDecimal(expected).compareTo(actual) == 0,
                "expected " + expected + " but was " + actual);
    }
}
