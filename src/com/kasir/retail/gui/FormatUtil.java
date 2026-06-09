package com.kasir.retail.gui;

import java.text.NumberFormat;
import java.util.Locale;

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
        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
        return "Rp" + nf.format(value);
    }
}
