package com.expensetracker.dao;

import com.expensetracker.model.Expense;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface for Expense entities.
 * 
 * OOP Concept: INTERFACE INHERITANCE & ABSTRACTION
 * Extends Repository<Expense> with rich query methods needed for
 * filtering, searching, dashboard summaries, and charts.
 */
public interface ExpenseDAO extends Repository<Expense> {

    /**
     * Finds all expenses belonging to a specific category.
     */
    List<Expense> findByCategory(int categoryId);

    /**
     * Finds all expenses recorded within a date range (inclusive).
     */
    List<Expense> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Dynamic search and filter across keyword note, category, and date range.
     */
    List<Expense> search(String keyword, Integer categoryId, LocalDate startDate, LocalDate endDate);

    /**
     * Computes the total spending for a specific month and year.
     */
    double getTotalExpensesByMonth(int month, int year);

    /**
     * Computes total spending across all records.
     */
    double getTotalExpensesAllTime();

    /**
     * Retrieves the N most recent expenses.
     */
    List<Expense> findRecent(int limit);
}
