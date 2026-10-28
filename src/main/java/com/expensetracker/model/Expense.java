package com.expensetracker.model;

import com.expensetracker.util.CurrencyFormatter;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Concrete model class representing an Expense.
 * 
 * OOP Concepts Demonstrated:
 * 1. INHERITANCE: Expense extends Transaction, inheriting 'id', 'amount', 'date', and 'note'.
 * 2. COMPOSITION / HAS-A RELATIONSHIP:
 *    - An Expense HAS-A Category (category)
 *    - An Expense HAS-A PaymentMethod (paymentMethod)
 * 3. POLYMORPHISM (Method Overriding):
 *    - Implements the abstract 'getDetails()' method from Transaction.
 *    - Overrides 'toString()' to provide a readable overview.
 * 4. ENCAPSULATION:
 *    - Private fields, defensive validation in setters, prevents null Category/PaymentMethod.
 */
public class Expense extends Transaction {

    private Category category;
    private PaymentMethod paymentMethod;

    // Default constructor
    public Expense() {
        super();
        this.paymentMethod = new CashPayment();
    }

    // Constructor without ID (for newly created expenses)
    public Expense(double amount, Category category, LocalDate date, PaymentMethod paymentMethod, String note) {
        super(amount, date, note);
        setCategory(category);
        setPaymentMethod(paymentMethod);
    }

    // Full constructor (for loading existing records from MySQL)
    public Expense(int id, double amount, Category category, LocalDate date, PaymentMethod paymentMethod, String note) {
        super(id, amount, date, note);
        setCategory(category);
        setPaymentMethod(paymentMethod);
    }

    // Polymorphism: Concrete implementation of the abstract contract from Transaction
    @Override
    public String getDetails() {
        String catName = (category != null) ? category.getName() : "Uncategorized";
        String pmName = (paymentMethod != null) ? paymentMethod.getDisplayName() : "Unknown";
        return String.format("Expense #%d: %s | %s | Paid via %s on %s%s",
                getId(),
                CurrencyFormatter.format(getAmount()),
                catName,
                pmName,
                getDate(),
                getNote().isBlank() ? "" : " (\"" + getNote() + "\")"
        );
    }

    // Getters and Setters with Encapsulation & Validation
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = Objects.requireNonNull(category, "Expense must be assigned a Category.");
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = (paymentMethod != null) ? paymentMethod : new CashPayment();
    }

    // Convenient getter for JavaFX TableView property binding
    public String getCategoryName() {
        return (category != null) ? category.getName() : "";
    }

    // Convenient getter for JavaFX TableView property binding
    public String getPaymentMethodName() {
        return (paymentMethod != null) ? paymentMethod.getDisplayName() : "";
    }

    // Convenient getter for formatted amount
    public String getFormattedAmount() {
        return CurrencyFormatter.format(getAmount());
    }

    @Override
    public String toString() {
        return getDetails();
    }
}
