package com.expensetracker.util;

import com.expensetracker.exception.InvalidAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying input validation logic and domain integrity.
 */
public class ValidatorTest {

    @Test
    @DisplayName("Should successfully parse clean positive amount")
    void testValidAmount() {
        double result = Validator.validateAmount("450.50");
        assertEquals(450.50, result, 0.001);
    }

    @Test
    @DisplayName("Should parse amount containing Rupee symbol, commas, and whitespace")
    void testAmountWithSymbolsAndCommas() {
        double result = Validator.validateAmount(" ₹ 1,50,000.75 ");
        assertEquals(150000.75, result, 0.001);
    }

    @Test
    @DisplayName("Should throw InvalidAmountException for zero amount")
    void testZeroAmountThrowsException() {
        InvalidAmountException ex = assertThrows(InvalidAmountException.class, () ->
                Validator.validateAmount("0")
        );
        assertTrue(ex.getMessage().contains("strictly greater than zero"));
    }

    @Test
    @DisplayName("Should throw InvalidAmountException for negative amount")
    void testNegativeAmountThrowsException() {
        assertThrows(InvalidAmountException.class, () ->
                Validator.validateAmount("-250.00")
        );
    }

    @Test
    @DisplayName("Should throw InvalidAmountException for empty string")
    void testEmptyAmountThrowsException() {
        assertThrows(InvalidAmountException.class, () ->
                Validator.validateAmount("   ")
        );
    }

    @Test
    @DisplayName("Should throw InvalidAmountException for non-numeric text")
    void testNonNumericAmountThrowsException() {
        assertThrows(InvalidAmountException.class, () ->
                Validator.validateAmount("one hundred")
        );
    }

    @Test
    @DisplayName("Should accept valid current or past date")
    void testValidDate() {
        LocalDate today = LocalDate.now();
        assertEquals(today, Validator.validateDate(today));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null date")
    void testNullDateThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                Validator.validateDate(null)
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for future date")
    void testFutureDateThrowsException() {
        LocalDate farFuture = LocalDate.now().plusMonths(6);
        assertThrows(IllegalArgumentException.class, () ->
                Validator.validateDate(farFuture)
        );
    }

    @Test
    @DisplayName("Should trim and sanitize note")
    void testSanitizeNote() {
        assertEquals("Dinner with family", Validator.sanitizeNote("  Dinner with family   "));
        assertEquals("", Validator.sanitizeNote(null));
    }
}
