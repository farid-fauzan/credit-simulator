package com.creditsimulator.domain;

import java.math.BigDecimal;

public class Motor extends Vehicle {

    private static final BigDecimal BASE_RATE = new BigDecimal("0.09");

    @Override
    public String getTypeName() {
        return "Motor";
    }

    @Override
    public BigDecimal getBaseInterestRate() {
        return BASE_RATE;
    }
}
