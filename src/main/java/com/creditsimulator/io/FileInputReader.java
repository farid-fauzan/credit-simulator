package com.creditsimulator.io;

import com.creditsimulator.domain.LoanRequest;
import com.creditsimulator.domain.Vehicle;
import com.creditsimulator.domain.VehicleCondition;
import com.creditsimulator.factory.VehicleFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a simple "key=value" per line file, e.g.:
 * jenisKendaraan=Mobil
 * kondisiKendaraan=Baru
 * tahunKendaraan=2025
 * jumlahPinjaman=1000000000
 * tenor=6
 * jumlahDP=500000000
 */
public class FileInputReader {

    public LoanRequest readLoanRequest(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        Map<String, String> values = new HashMap<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int separatorIndex = trimmed.indexOf('=');
            if (separatorIndex < 0) {
                continue;
            }
            String key = trimmed.substring(0, separatorIndex).trim();
            String value = trimmed.substring(separatorIndex + 1).trim();
            values.put(key, value);
        }

        Vehicle vehicle = VehicleFactory.create(require(values, "jenisKendaraan"));
        VehicleCondition condition = VehicleCondition.fromString(require(values, "kondisiKendaraan"));
        int vehicleYear = Integer.parseInt(require(values, "tahunKendaraan"));
        BigDecimal totalLoanAmount = new BigDecimal(require(values, "jumlahPinjaman"));
        int tenorYears = Integer.parseInt(require(values, "tenor"));
        BigDecimal downPayment = new BigDecimal(require(values, "jumlahDP"));

        return new LoanRequest(vehicle, condition, vehicleYear, totalLoanAmount, tenorYears, downPayment);
    }

    private String require(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Field wajib tidak ditemukan di file input: " + key);
        }
        return value;
    }
}
