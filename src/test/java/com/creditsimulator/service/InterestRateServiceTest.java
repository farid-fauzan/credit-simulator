package com.creditsimulator.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterestRateServiceTest {

    private final InterestRateService service = new InterestRateService();

    @Test
    void matchesSpecSampleForThreeYearTenor() {
        List<BigDecimal> rates = service.ratesForTenor(new BigDecimal("0.08"), 3);

        assertNumericallyEquals("0.080", rates.get(0));
        assertNumericallyEquals("0.081", rates.get(1));
        assertNumericallyEquals("0.086", rates.get(2));
    }

    @Test
    void singleYearTenorKeepsBaseRate() {
        List<BigDecimal> rates = service.ratesForTenor(new BigDecimal("0.09"), 1);

        assertEquals(1, rates.size());
        assertNumericallyEquals("0.09", rates.get(0));
    }

    private void assertNumericallyEquals(String expected, BigDecimal actual) {
        assertTrue(new BigDecimal(expected).compareTo(actual) == 0,
                "expected " + expected + " but was " + actual);
    }
}
