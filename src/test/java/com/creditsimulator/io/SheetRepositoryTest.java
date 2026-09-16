package com.creditsimulator.io;

import com.creditsimulator.domain.LoanRequest;
import com.creditsimulator.domain.Mobil;
import com.creditsimulator.domain.VehicleCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SheetRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void savesAndSwitchesBackToTheSameSheet() throws Exception {
        SheetRepository repository = new SheetRepository(tempDir.resolve("sheets"));
        LoanRequest original = new LoanRequest(
                new Mobil(), VehicleCondition.BARU, 2025,
                new BigDecimal("1000000000"), 6, new BigDecimal("500000000"));

        repository.save("mobil-baru", original);
        LoanRequest loaded = repository.load("mobil-baru");

        assertEquals(original.getVehicle().getTypeName(), loaded.getVehicle().getTypeName());
        assertEquals(original.getCondition(), loaded.getCondition());
        assertEquals(original.getVehicleYear(), loaded.getVehicleYear());
        assertEquals(0, original.getTotalLoanAmount().compareTo(loaded.getTotalLoanAmount()));
        assertEquals(original.getTenorYears(), loaded.getTenorYears());
        assertEquals(0, original.getDownPayment().compareTo(loaded.getDownPayment()));
    }

    @Test
    void listsSavedSheetNamesSorted() throws Exception {
        SheetRepository repository = new SheetRepository(tempDir.resolve("sheets"));
        LoanRequest request = new LoanRequest(
                new Mobil(), VehicleCondition.BEKAS, 2020,
                new BigDecimal("100000000"), 3, new BigDecimal("25000000"));

        repository.save("zeta", request);
        repository.save("alpha", request);

        List<String> names = repository.list();

        assertEquals(List.of("alpha", "zeta"), names);
    }

    @Test
    void listingWithNoSavedSheetsReturnsEmpty() throws Exception {
        SheetRepository repository = new SheetRepository(tempDir.resolve("sheets"));

        assertTrue(repository.list().isEmpty());
    }

    @Test
    void loadingAnUnknownSheetFails() {
        SheetRepository repository = new SheetRepository(tempDir.resolve("sheets"));

        assertThrows(IllegalArgumentException.class, () -> repository.load("does-not-exist"));
    }

    @Test
    void rejectsSheetNamesWithInvalidCharacters() {
        SheetRepository repository = new SheetRepository(tempDir.resolve("sheets"));
        LoanRequest request = new LoanRequest(
                new Mobil(), VehicleCondition.BEKAS, 2020,
                new BigDecimal("100000000"), 3, new BigDecimal("25000000"));

        assertThrows(IllegalArgumentException.class, () -> repository.save("../escape", request));
    }
}
