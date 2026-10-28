package com.expensetracker.exception;

/**
 * Thrown when an invalid transaction amount is provided (e.g., negative or zero).
 * 
 * OOP Concept: ENCAPSULATION & DOMAIN INTEGRITY
 * Why this class exists:
 * In OOP, an object must protect its own state. A transaction with amount <= 0
 * violates business rules. This custom exception enforces valid business state
 * whenever an expense or budget is created or modified.
 */
public class InvalidAmountException extends IllegalArgumentException {

    private final double invalidAmount;

    public InvalidAmountException(String message, double invalidAmount) {
        super(message + " (Provided amount: ₹" + invalidAmount + ")");
        this.invalidAmount = invalidAmount;
    }

    public InvalidAmountException(String message) {
        super(message);
        this.invalidAmount = 0.0;
    }

    public double getInvalidAmount() {
        return invalidAmount;
    }
}
