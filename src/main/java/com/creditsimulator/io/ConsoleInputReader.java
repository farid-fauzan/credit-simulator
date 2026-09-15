package com.creditsimulator.io;

import com.creditsimulator.domain.LoanRequest;
import com.creditsimulator.domain.Vehicle;
import com.creditsimulator.domain.VehicleCondition;
import com.creditsimulator.factory.VehicleFactory;

import java.io.BufferedReader;
import java.math.BigDecimal;

public class ConsoleInputReader {

    private final BufferedReader reader;

    public ConsoleInputReader(BufferedReader reader) {
        this.reader = reader;
    }

    public LoanRequest readLoanRequest() throws Exception {
        Vehicle vehicle = promptUntilValid("Jenis Kendaraan (Motor/Mobil): ", VehicleFactory::create);
        VehicleCondition condition = promptUntilValid("Kondisi Kendaraan (Bekas/Baru): ", VehicleCondition::fromString);
        int vehicleYear = promptUntilValid("Tahun Kendaraan (4 digit): ", ConsoleInputReader::parseYear);
        BigDecimal totalLoanAmount = promptUntilValid("Jumlah Pinjaman Total: ", ConsoleInputReader::parseAmount);
        int tenorYears = promptUntilValid("Tenor Pinjaman (1-6 thn): ", Integer::parseInt);
        BigDecimal downPayment = promptUntilValid("Jumlah DP: ", ConsoleInputReader::parseAmount);

        return new LoanRequest(vehicle, condition, vehicleYear, totalLoanAmount, tenorYears, downPayment);
    }

    private <T> T promptUntilValid(String prompt, ThrowingFunction<String, T> parser) throws Exception {
        while (true) {
            System.out.print(prompt);
            String line = reader.readLine();
            if (line == null) {
                throw new IllegalStateException("Input berakhir tanpa data");
            }
            try {
                return parser.apply(line.trim());
            } catch (Exception e) {
                System.out.println("Input tidak valid: " + e.getMessage());
            }
        }
    }

    private static int parseYear(String raw) {
        if (!raw.matches("\\d{4}")) {
            throw new IllegalArgumentException("Tahun kendaraan harus 4 digit angka");
        }
        return Integer.parseInt(raw);
    }

    private static BigDecimal parseAmount(String raw) {
        return new BigDecimal(raw.replace(",", ""));
    }

    @FunctionalInterface
    private interface ThrowingFunction<A, R> {
        R apply(A input) throws Exception;
    }
}
