package com.expensetracker.model;

/**
 * Enum representing accepted payment channels.
 * 
 * OOP Concept: TYPE SAFETY WITH ENUMS
 * Restricts payment methods to Cash, UPI, and Card.
 */
public enum PaymentType {
    CASH("Cash"),
    UPI("UPI"),
    CARD("Card");

    private final String displayName;

    PaymentType(String displayName) {
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
