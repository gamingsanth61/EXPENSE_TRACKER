package com.expensetracker.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CurrencyFormatterTest {

    @Test
    @DisplayName("Should format number with Indian Rupee symbol")
    void testFormatWithSymbol() {
        String formatted = CurrencyFormatter.format(1250.00);
        assertTrue(formatted.contains("₹"));
        assertTrue(formatted.contains("1,250.00"));
    }

    @Test
    @DisplayName("Should format large amounts with two decimal places")
    void testFormatLargeAmount() {
        String formatted = CurrencyFormatter.format(150000.00);
        assertTrue(formatted.contains("150,000.00") || formatted.contains("1,50,000.00"));
        assertTrue(formatted.contains("₹"));
    }
}
