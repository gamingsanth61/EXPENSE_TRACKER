package com.expensetracker.dao;

import com.expensetracker.model.Budget;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Budget entities.
 * 
 * OOP Concept: INTERFACE INHERITANCE
 */
public interface BudgetDAO extends Repository<Budget> {

    /**
     * Finds the budget allocation for a given category in a specific month and year.
     */
    Optional<Budget> findByCategoryAndMonthYear(int categoryId, int month, int year);

    /**
     * Finds all budgets set for a specific month and year.
     */
    List<Budget> findByMonthYear(int month, int year);
}
