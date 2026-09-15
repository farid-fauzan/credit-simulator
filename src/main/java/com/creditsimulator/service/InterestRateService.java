package com.creditsimulator.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Year-on-year rate progression, calibrated against the sample in the spec
 * (base 8% -> year2 8.1% -> year3 8.6%): each yearly transition alternates
 * between a +0.1% step and a +0.5% "every 2 years" step.
 */
public class InterestRateService {

    private static final BigDecimal STEP_YEARLY = new BigDecimal("0.001");
    private static final BigDecimal STEP_EVERY_TWO_YEARS = new BigDecimal("0.005");

    public List<BigDecimal> ratesForTenor(BigDecimal baseRate, int tenorYears) {
        List<BigDecimal> rates = new ArrayList<>();
        BigDecimal current = baseRate;
        rates.add(current);
        for (int year = 2; year <= tenorYears; year++) {
            int transition = year - 1;
            BigDecimal step = (transition % 2 == 1) ? STEP_YEARLY : STEP_EVERY_TWO_YEARS;
            current = current.add(step);
            rates.add(current);
        }
        return rates;
    }
}
