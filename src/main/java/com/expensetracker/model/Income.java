package com.expensetracker.model;

import com.expensetracker.util.CurrencyFormatter;

import java.time.LocalDate;

/**
 * Concrete model class representing an Income transaction.
 * 
 * OOP Concept: INHERITANCE & POLYMORPHISM
 * Shows that Transaction can have multiple subtypes (Expense, Income)
 * that each provide their own implementation of getDetails().
 */
public class Income extends Transaction {

    private String source;

    public Income() {
        super();
        this.source = "General Income";
    }

    public Income(double amount, String source, LocalDate date, String note) {
        super(amount, date, note);
        setSource(source);
    }

    public Income(int id, double amount, String source, LocalDate date, String note) {
        super(id, amount, date, note);
        setSource(source);
    }

    @Override
    public String getDetails() {
        return String.format("Income #%d: +%s from %s on %s",
                getId(),
                CurrencyFormatter.format(getAmount()),
                source,
                getDate());
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = (source != null && !source.isBlank()) ? source.trim() : "General Income";
    }
}
