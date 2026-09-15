package com.creditsimulator.validation;

import com.creditsimulator.domain.LoanRequest;
import com.creditsimulator.domain.VehicleCondition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class LoanValidator {

    private static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("1000000000");
    private static final BigDecimal DP_MIN_BARU = new BigDecimal("0.35");
    private static final BigDecimal DP_MIN_BEKAS = new BigDecimal("0.25");
    private static final int MAX_TENOR_YEARS = 6;

    public void validate(LoanRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getTenorYears() < 1 || request.getTenorYears() > MAX_TENOR_YEARS) {
            errors.add("Tenor pinjaman harus antara 1-" + MAX_TENOR_YEARS + " tahun");
        }

        if (request.getTotalLoanAmount() == null || request.getTotalLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Jumlah pinjaman harus lebih besar dari 0");
        } else if (request.getTotalLoanAmount().compareTo(MAX_LOAN_AMOUNT) > 0) {
            errors.add("Jumlah pinjaman tidak boleh lebih dari Rp 1.000.000.000");
        }

        if (request.getDownPayment() == null || request.getDownPayment().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Jumlah DP tidak boleh negatif");
        } else if (request.getTotalLoanAmount() != null
                && request.getDownPayment().compareTo(request.getTotalLoanAmount()) >= 0) {
            errors.add("Jumlah DP harus lebih kecil dari jumlah pinjaman");
        }

        if (request.getCondition() == VehicleCondition.BARU) {
            int minYear = Year.now().getValue() - 1;
            if (request.getVehicleYear() < minYear) {
                errors.add("Kendaraan baru tidak boleh tahun kurang dari " + minYear);
            }
        }

        if (request.getTotalLoanAmount() != null
                && request.getTotalLoanAmount().compareTo(BigDecimal.ZERO) > 0
                && request.getDownPayment() != null) {
            BigDecimal dpPercentage = request.getDownPayment()
                    .divide(request.getTotalLoanAmount(), 4, RoundingMode.HALF_UP);
            BigDecimal minDpPercentage = request.getCondition() == VehicleCondition.BARU
                    ? DP_MIN_BARU
                    : DP_MIN_BEKAS;
            if (dpPercentage.compareTo(minDpPercentage) < 0) {
                errors.add(String.format(
                        "Jumlah DP kendaraan %s minimal %.0f%% dari jumlah pinjaman",
                        request.getCondition() == VehicleCondition.BARU ? "baru" : "bekas",
                        minDpPercentage.multiply(BigDecimal.valueOf(100))));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(String.join("; ", errors));
        }
    }
}
