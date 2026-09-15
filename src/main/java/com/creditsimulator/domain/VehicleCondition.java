package com.creditsimulator.domain;

public enum VehicleCondition {
    BARU,
    BEKAS;

    public static VehicleCondition fromString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Kondisi kendaraan tidak boleh kosong");
        }
        String normalized = raw.trim().toUpperCase();
        if (normalized.equals("BARU")) return BARU;
        if (normalized.equals("BEKAS")) return BEKAS;
        throw new IllegalArgumentException("Kondisi kendaraan tidak valid: '" + raw + "' (harus Baru atau Bekas)");
    }
}
