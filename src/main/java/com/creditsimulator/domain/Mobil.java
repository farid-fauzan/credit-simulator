package com.creditsimulator.domain;

import java.math.BigDecimal;

public class Mobil extends Vehicle {

    private static final BigDecimal BASE_RATE = new BigDecimal("0.08");

    @Override
    public String getTypeName() {
        return "Mobil";
    }

    @Override
    public BigDecimal getBaseInterestRate() {
        return BASE_RATE;
    }
}
