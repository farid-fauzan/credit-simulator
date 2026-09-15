package com.creditsimulator.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormatter {

    private static final DecimalFormat FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    private MoneyFormatter() {
    }

    public static String toRupiah(BigDecimal amount) {
        return "Rp. " + FORMAT.format(amount);
    }

    public static String toPercentage(BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%";
    }
}
