package com.expensetracker.model;

/**
 * Concrete implementation of PaymentMethod for cash transactions.
 * 
 * OOP Concept: POLYMORPHISM (Method Overriding & Interface Implementation)
 */
public class CashPayment implements PaymentMethod {

    @Override
    public PaymentType getType() {
        return PaymentType.CASH;
    }

    @Override
    public String getDisplayName() {
        return "Cash";
    }

    @Override
    public String getIcon() {
        return "💵";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CashPayment;
    }

    @Override
    public int hashCode() {
        return PaymentType.CASH.hashCode();
    }
}
