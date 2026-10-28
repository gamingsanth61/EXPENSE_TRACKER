package com.expensetracker.model;

/**
 * Enum representing category classifications.
 * 
 * OOP Concept: TYPE SAFETY WITH ENUMS
 * Why this class exists:
 * Instead of using raw strings ("EXPENSE", "INCOME") which can suffer from typos
 * like "Expence", Enums enforce compile-time type safety and restrict values to
 * a predefined valid set.
 */
public enum CategoryType {
    EXPENSE("Expense"),
    INCOME("Income");

    private final String displayName;

    CategoryType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
