package com.expensetracker.util;

import com.expensetracker.exception.InvalidAmountException;

import java.time.LocalDate;

/**
 * Utility class for validating user input before creating domain models or running queries.
 * 
 * OOP Concept: SINGLE RESPONSIBILITY PRINCIPLE (SRP) & DEFENSIVE PROGRAMMING
 * Why this class exists:
 * Controllers handle UI events, and Models enforce their own invariants.
 * However, validating raw user input from text fields (checking if text is numeric,
 * checking string length, bounds, etc.) is a shared concern. Centralizing validation
 * logic in a Validator utility keeps controllers concise and prevents invalid data
 * from reaching the Service or DAO layers.
 */
public final class Validator {

    private Validator() {
        throw new UnsupportedOperationException("Validator is a utility class and cannot be instantiated.");
    }

    /**
     * Parses and validates an amount string from a text field.
     *
     * @param amountStr The raw string from the UI TextField
     * @return The parsed positive double
     * @throws InvalidAmountException if empty, non-numeric, or <= 0
     */
    public static double validateAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new InvalidAmountException("Amount cannot be empty.");
        }

        // Remove possible currency symbol, commas, or whitespace
        String cleaned = amountStr.replace("₹", "").replace(",", "").trim();

        double amount;
        try {
            amount = Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Please enter a valid numeric amount (e.g., 250 or 1499.50).");
        }

        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be strictly greater than zero.", amount);
        }

        // Limit to 2 decimal places precision for financial integrity
        return Math.round(amount * 100.0) / 100.0;
    }

    /**
     * Validates that a date is selected and not in the distant future.
     */
    public static LocalDate validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Please select a valid transaction date.");
        }
        if (date.isAfter(LocalDate.now().plusDays(1))) {
            throw new IllegalArgumentException("Transaction date cannot be in the future.");
        }
        return date;
    }

    /**
     * Validates and cleans optional transaction note.
     */
    public static String sanitizeNote(String note) {
        if (note == null) {
            return "";
        }
        String trimmed = note.trim();
        if (trimmed.length() > 255) {
            throw new IllegalArgumentException("Note cannot exceed 255 characters (current: " + trimmed.length() + ").");
        }
        return trimmed;
    }
}
