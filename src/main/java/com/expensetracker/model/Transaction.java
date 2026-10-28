package com.expensetracker.model;

import com.expensetracker.exception.InvalidAmountException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract base class representing any financial movement (Expense, Income, etc.).
 * 
 * OOP Concept: ABSTRACTION & ENCAPSULATION
 * Why this class exists:
 * 1. Abstraction: Defines the common template for all transactions (id, amount, date, note)
 *    and specifies abstract contracts (like getDetails()) that sub-classes MUST implement.
 *    You cannot instantiate a generic "Transaction" directly because every real-world
 *    transaction is a specific type (e.g. an Expense).
 * 2. Encapsulation: Fields are private and guarded by setters that enforce domain rules
 *    (e.g., throwing InvalidAmountException if amount <= 0).
 */
public abstract class Transaction {

    private int id;
    private double amount;
    private LocalDate date;
    private String note;

    // Default constructor
    public Transaction() {
        this.date = LocalDate.now();
        this.note = "";
    }

    // Parameterized constructor
    public Transaction(double amount, LocalDate date, String note) {
        setAmount(amount);
        setDate(date);
        setNote(note);
    }

    // Full constructor (including DB primary key id)
    public Transaction(int id, double amount, LocalDate date, String note) {
        this.id = id;
        setAmount(amount);
        setDate(date);
        setNote(note);
    }

    // Abstract method: Every subclass MUST define how it formats its specific details
    public abstract String getDetails();

    // Getters and Setters with Encapsulation & Validation
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Transaction amount must be strictly greater than zero.", amount);
        }
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = (date != null) ? date : LocalDate.now();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = (note != null) ? note.trim() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
