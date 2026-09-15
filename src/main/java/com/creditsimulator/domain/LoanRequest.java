package com.creditsimulator.domain;

import java.math.BigDecimal;

public class LoanRequest {

    private final Vehicle vehicle;
    private final VehicleCondition condition;
    private final int vehicleYear;
    private final BigDecimal totalLoanAmount;
    private final int tenorYears;
    private final BigDecimal downPayment;

    public LoanRequest(Vehicle vehicle, VehicleCondition condition, int vehicleYear,
                        BigDecimal totalLoanAmount, int tenorYears, BigDecimal downPayment) {
        this.vehicle = vehicle;
        this.condition = condition;
        this.vehicleYear = vehicleYear;
        this.totalLoanAmount = totalLoanAmount;
        this.tenorYears = tenorYears;
        this.downPayment = downPayment;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public VehicleCondition getCondition() {
        return condition;
    }

    public int getVehicleYear() {
        return vehicleYear;
    }

    public BigDecimal getTotalLoanAmount() {
        return totalLoanAmount;
    }

    public int getTenorYears() {
        return tenorYears;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }
}
