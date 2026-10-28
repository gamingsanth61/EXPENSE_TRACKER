package com.expensetracker.exception;

/**
 * Thrown when an expense causes a category to exceed its monthly budget limit.
 * 
 * OOP Concept: CUSTOM DOMAIN EXCEPTION
 * Why this class exists:
 * Expressive exception handling allows the application to distinguish between
 * technical failures (like DatabaseException) and business rule violations
 * (like exceeding a budget). This lets the UI present helpful warnings to the user.
 */
public class BudgetExceededException extends Exception {

    private final String categoryName;
    private final double budgetLimit;
    private final double attemptedTotal;

    public BudgetExceededException(String categoryName, double budgetLimit, double attemptedTotal) {
        super(String.format("Budget warning: Adding this expense will exceed the monthly budget for '%s' "
                + "(Limit: ₹%.2f, New Total: ₹%.2f)", categoryName, budgetLimit, attemptedTotal));
        this.categoryName = categoryName;
        this.budgetLimit = budgetLimit;
        this.attemptedTotal = attemptedTotal;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public double getAttemptedTotal() {
        return attemptedTotal;
    }
}
