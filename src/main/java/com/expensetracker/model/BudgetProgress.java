package com.expensetracker.model;

import com.expensetracker.util.CurrencyFormatter;

/**
 * Encapsulates the runtime progress and status of a category's budget.
 * 
 * OOP Concept: ENCAPSULATION & DATA TRANSFER OBJECT (DTO)
 * Why this class exists:
 * The database 'budgets' table only stores the limit, month, and year.
 * However, the UI needs to display dynamic calculations:
 * - How much has been spent so far?
 * - How much balance remains?
 * - What percentage of the budget is consumed?
 * - Has the budget been exceeded?
 * Encapsulating these derived metrics in a dedicated class keeps the domain
 * model clean and provides ready-to-display values for the presentation layer.
 */
public class BudgetProgress {

    private final Budget budget;
    private final double spent;

    public BudgetProgress(Budget budget, double spent) {
        this.budget = budget;
        this.spent = spent;
    }

    public Budget getBudget() {
        return budget;
    }

    public double getLimit() {
        return budget.getMonthlyLimit();
    }

    public double getSpent() {
        return spent;
    }

    public double getRemaining() {
        return Math.max(0.0, getLimit() - spent);
    }

    public double getExceededAmount() {
        return Math.max(0.0, spent - getLimit());
    }

    /**
     * Percentage spent (e.g. 75.5%). Can exceed 100%.
     */
    public double getPercentage() {
        if (getLimit() <= 0) return 0.0;
        return (spent / getLimit()) * 100.0;
    }

    /**
     * Clamped fraction for JavaFX ProgressBar (0.0 to 1.0).
     */
    public double getProgressFraction() {
        if (getLimit() <= 0) return 0.0;
        return Math.min(1.0, spent / getLimit());
    }

    public boolean isExceeded() {
        return spent > getLimit();
    }

    public boolean isNearLimit() {
        double pct = getPercentage();
        return pct >= 80.0 && pct <= 100.0;
    }

    public String getFormattedLimit() {
        return CurrencyFormatter.format(getLimit());
    }

    public String getFormattedSpent() {
        return CurrencyFormatter.format(spent);
    }

    public String getFormattedRemaining() {
        return CurrencyFormatter.format(getRemaining());
    }

    public String getFormattedExceeded() {
        return CurrencyFormatter.format(getExceededAmount());
    }

    public String getCategoryName() {
        return budget.getCategoryName();
    }
}
