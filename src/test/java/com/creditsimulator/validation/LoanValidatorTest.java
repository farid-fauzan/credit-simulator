package com.creditsimulator.validation;

import com.creditsimulator.domain.LoanRequest;
import com.creditsimulator.domain.Mobil;
import com.creditsimulator.domain.VehicleCondition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanValidatorTest {

    private final LoanValidator validator = new LoanValidator();

    @Test
    void acceptsValidUsedVehicleRequest() {
        LoanRequest request = new LoanRequest(
                new Mobil(), VehicleCondition.BEKAS, 2020,
                new BigDecimal("100000000"), 3, new BigDecimal("25000000"));

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void rejectsTenorAboveSixYears() {
        LoanRequest request = new LoanRequest(
                new Mobil(), VehicleCondition.BEKAS, 2020,
                new BigDecimal("100000000"), 7, new BigDecimal("25000000"));

        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void rejectsLoanAmountAboveOneBillion() {
        LoanRequest request = new LoanRequest(
                new Mobil(), VehicleCondition.BEKAS, 2020,
                new BigDecimal("1000000001"), 3, new BigDecimal("300000000"));

        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void rejectsDownPaymentBelowMinimumForNewVehicle() {
        LoanRequest request = new LoanRequest(
                new Mobil(), VehicleCondition.BARU, Year.now().getValue(),
                new BigDecimal("100000000"), 3, new BigDecimal("30000000"));

        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void rejectsDownPaymentBelowMinimumForUsedVehicle() {
        LoanRequest request = new LoanRequest(
                new Mobil(), VehicleCondition.BEKAS, 2020,
                new BigDecimal("100000000"), 3, new BigDecimal("20000000"));

        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void rejectsNewVehicleOlderThanCurrentYearMinusOne() {
        LoanRequest request = new LoanRequest(
                new Mobil(), VehicleCondition.BARU, Year.now().getValue() - 2,
                new BigDecimal("100000000"), 3, new BigDecimal("35000000"));

        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
}
