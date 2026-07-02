package com.kasir.retail.gui;

import java.text.NumberFormat;

public class FormatUtil {

    public static double parseIndonesianNumber(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new NumberFormatException("Input kosong");
        }
        String cleaned = input.trim()
            .replace("Rp", "")
            .replace("rp", "")
            .replace(" ", "")
            .replace(".", "");
        cleaned = cleaned.replace(",", ".");
        return Double.parseDouble(cleaned);
    }

    public static String formatRupiah(double value) {
        NumberFormat nf = NumberFormat.getInstance(new java.util.Locale.Builder().setLanguage("id").setRegion("ID").build());
        return "Rp" + nf.format(value);
    }
}
