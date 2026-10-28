package com.expensetracker.service;

import com.expensetracker.dao.*;
import com.expensetracker.model.Budget;
import com.expensetracker.model.BudgetProgress;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.util.Validator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class orchestrating budget management and progress tracking.
 * 
 * OOP Concepts: SERVICE LAYER PATTERN & ENCAPSULATED AGGREGATIONS
 */
public class BudgetService {

    private final BudgetDAO budgetDAO;
    private final ExpenseDAO expenseDAO;
    private final CategoryDAO categoryDAO;

    public BudgetService() {
        this(new BudgetDAOImpl(), new ExpenseDAOImpl(), new CategoryDAOImpl());
    }

    public BudgetService(BudgetDAO budgetDAO, ExpenseDAO expenseDAO, CategoryDAO categoryDAO) {
        this.budgetDAO = budgetDAO;
        this.expenseDAO = expenseDAO;
        this.categoryDAO = categoryDAO;
    }

    /**
     * Sets or updates a category budget for a specific month and year.
     *
     * @param category Selected category
     * @param limitStr Raw limit string (validated with Validator)
     * @param month    Month (1-12)
     * @param year     Year (>= 2000)
     * @return Saved Budget entity
     */
    public Budget setBudget(Category category, String limitStr, int month, int year) {
        if (category == null) {
            throw new IllegalArgumentException("Please select a Category.");
        }
        double limit = Validator.validateAmount(limitStr);

        Budget budget = new Budget(category, limit, month, year);
        return budgetDAO.save(budget);
    }

    /**
     * Computes the budget progress for all configured categories in the given month and year.
     *
     * @param month Month (1-12)
     * @param year  Year
     * @return List of BudgetProgress DTOs with calculated metrics
     */
    public List<BudgetProgress> getBudgetProgressList(int month, int year) {
        List<Budget> budgets = budgetDAO.findByMonthYear(month, year);
        List<BudgetProgress> progressList = new ArrayList<>();

        for (Budget b : budgets) {
            // Find all expenses for this category in this month and year
            List<Expense> categoryExpenses = expenseDAO.findByCategory(b.getCategory().getId());
            double spent = categoryExpenses.stream()
                    .filter(e -> e.getDate().getMonthValue() == month && e.getDate().getYear() == year)
                    .mapToDouble(Expense::getAmount)
                    .sum();

            progressList.add(new BudgetProgress(b, spent));
        }
        return progressList;
    }

    /**
     * Deletes a budget entry by ID.
     */
    public boolean deleteBudget(int id) {
        return budgetDAO.delete(id);
    }

    /**
     * Retrieves all categories for the dropdown.
     */
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }
}
