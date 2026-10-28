package com.expensetracker.model;

/**
 * Concrete implementation of PaymentMethod for Debit/Credit Cards.
 * 
 * OOP Concept: POLYMORPHISM (Method Overriding & Interface Implementation)
 */
public class CardPayment implements PaymentMethod {

    @Override
    public PaymentType getType() {
        return PaymentType.CARD;
    }

    @Override
    public String getDisplayName() {
        return "Card";
    }

    @Override
    public String getIcon() {
        return "💳";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CardPayment;
    }

    @Override
    public int hashCode() {
        return PaymentType.CARD.hashCode();
    }
}
