package com.expensetracker.model;

/**
 * Interface defining contract for payment methods.
 * 
 * OOP Concept: ABSTRACTION & POLYMORPHISM (Interface)
 * Why this interface exists:
 * An expense does not need to know the specific mechanics of Cash, UPI, or Card.
 * It only cares that a PaymentMethod can identify its type, provide a user-friendly
 * description, and format itself. Different payment strategies can be introduced
 * (e.g., NetBanking, Crypto) without modifying the Expense class (Open/Closed Principle).
 */
public interface PaymentMethod {

    /**
     * Returns the standardized payment type enum.
     */
    PaymentType getType();

    /**
     * Returns the human-readable display name.
     */
    String getDisplayName();

    /**
     * Returns a short icon or symbol identifier for the UI.
     */
    String getIcon();

    /**
     * Static factory method to instantiate the correct PaymentMethod implementation
     * based on the PaymentType enum.
     * 
     * OOP Concept: FACTORY METHOD PATTERN
     */
    static PaymentMethod fromType(PaymentType type) {
        if (type == null) {
            return new CashPayment();
        }
        return switch (type) {
            case CASH -> new CashPayment();
            case UPI -> new UpiPayment();
            case CARD -> new CardPayment();
        };
    }

    /**
     * Static helper to parse a string representation (e.g., from DB).
     */
    static PaymentMethod fromString(String name) {
        if (name == null || name.isBlank()) {
            return new CashPayment();
        }
        try {
            PaymentType type = PaymentType.valueOf(name.trim().toUpperCase());
            return fromType(type);
        } catch (IllegalArgumentException e) {
            return new CashPayment();
        }
    }
}
