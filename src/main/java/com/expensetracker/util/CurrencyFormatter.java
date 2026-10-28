package com.expensetracker.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class to format numbers into Indian Rupee (₹) currency representations.
 * 
 * OOP Concept: SINGLE RESPONSIBILITY PRINCIPLE (SRP) & UTILITY CLASS
 * Why this class exists:
 * Formatting currency is a presentation concern, not a model concern.
 * Storing amounts as primitive doubles or BigDecimals in models while centralizing
 * string formatting here keeps models pure and presentation consistent across the app.
 */
public final class CurrencyFormatter {

    // Indian Rupee Symbol
    public static final String RUPEE_SYMBOL = "₹";

    // Private constructor prevents instantiation of utility class
    private CurrencyFormatter() {
        throw_unsupported();
    }

    private static void throw_unsupported() {
        throw new UnsupportedOperationException("CurrencyFormatter is a utility class and cannot be instantiated.");
    }

    /**
     * Formats a double value to Indian Rupee format, e.g. "₹ 1,250.00" or "₹ 1,50,000.00".
     *
     * @param amount The numeric amount to format
     * @return Formatted currency string with ₹ symbol
     */
    public static String format(double amount) {
        // DecimalFormat with Indian numbering grouping (lakhs/crores)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("en", "IN"));
        symbols.setCurrencySymbol(RUPEE_SYMBOL);
        
        // Standard Indian grouping pattern: ##,##,##0.00
        DecimalFormat formatter = new DecimalFormat("₹ #,##,##0.00", symbols);
        return formatter.format(amount);
    }

    /**
     * Formats a double value without the ₹ symbol, e.g. "1,250.00".
     */
    public static String formatWithoutSymbol(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("en", "IN"));
        DecimalFormat formatter = new DecimalFormat("#,##,##0.00", symbols);
        return formatter.format(amount);
    }
}
