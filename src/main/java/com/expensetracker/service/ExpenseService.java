package com.expensetracker.service;

import com.expensetracker.dao.BudgetDAO;
import com.expensetracker.dao.BudgetDAOImpl;
import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.dao.CategoryDAOImpl;
import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.dao.ExpenseDAOImpl;
import com.expensetracker.exception.BudgetExceededException;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.PaymentMethod;
import com.expensetracker.util.Validator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class orchestrating business logic for Expense operations.
 * 
 * OOP Concepts: SERVICE LAYER PATTERN, DEPENDENCY INVERSION & BUSINESS LOGIC ENCAPSULATION
 * Why this class exists:
 * 1. Separation of Concerns: Neither the Controller (JavaFX UI) nor the DAO (SQL queries)
 *    should contain business rules. The Service layer bridges the two.
 * 2. Orchestration: When adding an expense, the service validates input, persists the record
 *    via ExpenseDAO, and checks whether the new expense exceeds the category's monthly budget
 *    threshold via BudgetDAO.
 */
public class ExpenseService {

    private final ExpenseDAO expenseDAO;
    private final CategoryDAO categoryDAO;
    private final BudgetDAO budgetDAO;

    // Default constructor instantiates default DAO implementations
    public ExpenseService() {
        this(new ExpenseDAOImpl(), new CategoryDAOImpl(), new BudgetDAOImpl());
    }

    // Parameterized constructor allows dependency injection (useful for unit testing / mocking)
    public ExpenseService(ExpenseDAO expenseDAO, CategoryDAO categoryDAO, BudgetDAO budgetDAO) {
        this.expenseDAO = expenseDAO;
        this.categoryDAO = categoryDAO;
        this.budgetDAO = budgetDAO;
    }

    /**
     * Creates and records a new Expense.
     * Checks if this addition causes the category to exceed its monthly budget.
     *
     * @param amountStr     Raw amount string from UI
     * @param category      Selected Category
     * @param date          Selected transaction date
     * @param paymentMethod Selected PaymentMethod (Cash/UPI/Card)
     * @param rawNote       Optional user note
     * @return The persisted Expense entity with generated ID
     * @throws BudgetExceededException if expense exceeds the monthly budget
     */
    public Expense addExpense(String amountStr, Category category, LocalDate date,
                              PaymentMethod paymentMethod, String rawNote) throws BudgetExceededException {
        // Step 1: Input Validation
        double amount = Validator.validateAmount(amountStr);
        LocalDate validDate = Validator.validateDate(date);
        String cleanNote = Validator.sanitizeNote(rawNote);

        if (category == null) {
            throw new IllegalArgumentException("Please select a Category for the expense.");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Please select a Payment Method.");
        }

        // Step 2: Construct the domain model
        Expense expense = new Expense(amount, category, validDate, paymentMethod, cleanNote);

        // Step 3: Persist via DAO
        Expense savedExpense = expenseDAO.save(expense);

        // Step 4: Business Rule - Check budget warning
        checkBudgetLimit(category, validDate.getMonthValue(), validDate.getYear());

        return savedExpense;
    }

    /**
     * Checks if total monthly spending for a category exceeds its assigned budget.
     */
    public void checkBudgetLimit(Category category, int month, int year) throws BudgetExceededException {
        Optional<Budget> budgetOpt = budgetDAO.findByCategoryAndMonthYear(category.getId(), month, year);
        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            // Calculate current total for this category in this month
            List<Expense> categoryExpenses = expenseDAO.findByCategory(category.getId());
            double totalSpentInMonth = categoryExpenses.stream()
                    .filter(e -> e.getDate().getMonthValue() == month && e.getDate().getYear() == year)
                    .mapToDouble(Expense::getAmount)
                    .sum();

            if (totalSpentInMonth > budget.getMonthlyLimit()) {
                throw new BudgetExceededException(category.getName(), budget.getMonthlyLimit(), totalSpentInMonth);
            }
        }
    }

    /**
     * Updates an existing Expense.
     *
     * @param id            Primary key ID of the expense to update
     * @param amountStr     Raw amount string from UI
     * @param category      Selected Category
     * @param date          Selected transaction date
     * @param paymentMethod Selected PaymentMethod
     * @param rawNote       Optional user note
     * @return true if update succeeded
     */
    public boolean updateExpense(int id, String amountStr, Category category, LocalDate date,
                                 PaymentMethod paymentMethod, String rawNote) throws BudgetExceededException {
        double amount = Validator.validateAmount(amountStr);
        LocalDate validDate = Validator.validateDate(date);
        String cleanNote = Validator.sanitizeNote(rawNote);

        if (category == null) {
            throw new IllegalArgumentException("Please select a Category.");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Please select a Payment Method.");
        }

        Expense expense = new Expense(id, amount, category, validDate, paymentMethod, cleanNote);
        boolean updated = expenseDAO.update(expense);

        if (updated) {
            checkBudgetLimit(category, validDate.getMonthValue(), validDate.getYear());
        }
        return updated;
    }

    /**
     * Deletes an expense by ID.
     */
    public boolean deleteExpense(int id) {
        return expenseDAO.delete(id);
    }

    /**
     * Filters and searches expenses based on keyword, category, and date range.
     *
     * OOP Concept: STREAMS & COLLECTIONS
     * Coordinates query execution and returns immutable/filtered lists.
     */
    public List<Expense> searchExpenses(String keyword, Integer categoryId, LocalDate startDate, LocalDate endDate) {
        return expenseDAO.search(keyword, categoryId, startDate, endDate);
    }

    /**
     * Computes the total sum of the given list of expenses using Java 8+ Streams.
     *
     * OOP Concept: FUNCTIONAL STREAMS & COLLECTIONS
     */
    public double calculateTotalAmount(List<Expense> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            return 0.0;
        }
        return expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    /**
     * Fetches all available categories for dropdown selection.
     */
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    /**
     * Fetches all recorded expenses.
     */
    public List<Expense> getAllExpenses() {
        return expenseDAO.findAll();
    }

    /**
     * Fetches recent expenses for the dashboard preview.
     */
    public List<Expense> getRecentExpenses(int limit) {
        return expenseDAO.findRecent(limit);
    }
}
