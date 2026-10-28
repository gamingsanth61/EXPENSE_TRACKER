package com.expensetracker.model;

/**
 * Concrete implementation of PaymentMethod for UPI payments (Google Pay, PhonePe, Paytm, etc.).
 * 
 * OOP Concept: POLYMORPHISM (Method Overriding & Interface Implementation)
 */
public class UpiPayment implements PaymentMethod {

    @Override
    public PaymentType getType() {
        return PaymentType.UPI;
    }

    @Override
    public String getDisplayName() {
        return "UPI";
    }

    @Override
    public String getIcon() {
        return "📱";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UpiPayment;
    }

    @Override
    public int hashCode() {
        return PaymentType.UPI.hashCode();
    }
}
