package com.creditsimulator.domain;

import java.math.BigDecimal;

public abstract class Vehicle {

    public abstract String getTypeName();

    public abstract BigDecimal getBaseInterestRate();
}
