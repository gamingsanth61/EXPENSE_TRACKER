package com.expensetracker.model;

import com.expensetracker.exception.InvalidAmountException;
import com.expensetracker.util.CurrencyFormatter;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

/**
 * Model class representing a monthly budget allocation for a category.
 * 
 * OOP Concept: ENCAPSULATION & COMPOSITION
 * - Encapsulation: Ensures monthly limit is positive and month is between 1 and 12.
 * - Composition: Budget HAS-A Category.
 */
public class Budget {

    private int id;
    private Category category;
    private double monthlyLimit;
    private int month; // 1 = January, 12 = December
    private int year;

    public Budget() {}

    public Budget(Category category, double monthlyLimit, int month, int year) {
        setCategory(category);
        setMonthlyLimit(monthlyLimit);
        setMonth(month);
        setYear(year);
    }

    public Budget(int id, Category category, double monthlyLimit, int month, int year) {
        this.id = id;
        setCategory(category);
        setMonthlyLimit(monthlyLimit);
        setMonth(month);
        setYear(year);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = Objects.requireNonNull(category, "Budget must have an associated Category.");
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        if (monthlyLimit <= 0) {
            throw new InvalidAmountException("Budget monthly limit must be greater than zero.", monthlyLimit);
        }
        this.monthlyLimit = monthlyLimit;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12.");
        }
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        if (year < 2000) {
            throw new IllegalArgumentException("Year must be 2000 or later.");
        }
        this.year = year;
    }

    public String getMonthName() {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public String getFormattedLimit() {
        return CurrencyFormatter.format(monthlyLimit);
    }

    public String getCategoryName() {
        return (category != null) ? category.getName() : "";
    }

    @Override
    public String toString() {
        return String.format("Budget [%s - %s %d: %s]",
                getCategoryName(), getMonthName(), year, getFormattedLimit());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Budget budget = (Budget) o;
        return id == budget.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
