package com.creditsimulator.factory;

import com.creditsimulator.domain.Mobil;
import com.creditsimulator.domain.Motor;
import com.creditsimulator.domain.Vehicle;

public final class VehicleFactory {

    private VehicleFactory() {
    }

    public static Vehicle create(String rawType) {
        if (rawType == null) {
            throw new IllegalArgumentException("Jenis kendaraan tidak boleh kosong");
        }
        String normalized = rawType.trim().toUpperCase();
        switch (normalized) {
            case "MOBIL":
                return new Mobil();
            case "MOTOR":
                return new Motor();
            default:
                throw new IllegalArgumentException(
                        "Jenis kendaraan tidak valid: '" + rawType + "' (harus Mobil atau Motor)");
        }
    }
}
