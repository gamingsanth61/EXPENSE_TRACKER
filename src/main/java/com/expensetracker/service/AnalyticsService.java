package com.expensetracker.service;

import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.dao.ExpenseDAOImpl;
import com.expensetracker.model.Expense;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class dedicated to computing statistical and analytical metrics.
 * 
 * OOP Concepts Demonstrated:
 * 1. SINGLE RESPONSIBILITY PRINCIPLE (SRP): Analytics calculations (groupings, trends,
 *    top category evaluations) are decoupled from CRUD operations.
 * 2. COLLECTIONS & GENERICS: Uses Map<String, Double>, List<Expense>, and Optional<Map.Entry<...>>.
 * 3. FUNCTIONAL STREAMS: Employs groupingBy, summingDouble, and stream comparators.
 */
public class AnalyticsService {

    private final ExpenseDAO expenseDAO;

    public AnalyticsService() {
        this(new ExpenseDAOImpl());
    }

    public AnalyticsService(ExpenseDAO expenseDAO) {
        this.expenseDAO = expenseDAO;
    }

    /**
     * Retrieves all-time total spending.
     */
    public double getTotalSpendingAllTime() {
        return expenseDAO.getTotalExpensesAllTime();
    }

    /**
     * Retrieves total spending for the current month and year.
     */
    public double getCurrentMonthSpending() {
        LocalDate now = LocalDate.now();
        return expenseDAO.getTotalExpensesByMonth(now.getMonthValue(), now.getYear());
    }

    /**
     * Computes the top spending category across all expenses.
     * Demonstrates Java Streams with Map entry comparator.
     *
     * @return An Optional containing the Map.Entry (Category Name -> Total Amount Spent)
     */
    public Optional<Map.Entry<String, Double>> getTopSpendingCategory() {
        List<Expense> allExpenses = expenseDAO.findAll();
        if (allExpenses.isEmpty()) {
            return Optional.empty();
        }

        // Group by Category Name and sum amounts using Java Streams
        Map<String, Double> categoryTotals = allExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategoryName,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        // Find the category entry with the maximum spending value
        return categoryTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    /**
     * Returns a Map of Category Name to Total Spending.
     */
    public Map<String, Double> getCategoryWiseSpending() {
        List<Expense> allExpenses = expenseDAO.findAll();
        return allExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategoryName,
                        LinkedHashMap::new,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    /**
     * Computes monthly spending breakdown for a specific year (e.g. "January" -> 4500.0).
     */
    public Map<String, Double> getMonthlyBreakdown(int year) {
        List<Expense> allExpenses = expenseDAO.findAll();
        Map<Integer, Double> monthNumMap = allExpenses.stream()
                .filter(e -> e.getDate().getYear() == year)
                .collect(Collectors.groupingBy(
                        e -> e.getDate().getMonthValue(),
                        Collectors.summingDouble(Expense::getAmount)
                ));

        Map<String, Double> sortedMonthly = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            String monthName = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            sortedMonthly.put(monthName, monthNumMap.getOrDefault(m, 0.0));
        }
        return sortedMonthly;
    }

    /**
     * Returns a Map of Category Name to Total Spending for a given year.
     */
    public Map<String, Double> getCategoryWiseSpending(int year) {
        List<Expense> allExpenses = expenseDAO.findAll();
        return allExpenses.stream()
                .filter(e -> e.getDate().getYear() == year)
                .collect(Collectors.groupingBy(
                        Expense::getCategoryName,
                        LinkedHashMap::new,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    /**
     * Returns cumulative monthly spending trends for a given year.
     * Maps each month to the cumulative expenditure up to that month.
     */
    public Map<String, Double> getCumulativeSpendingTrend(int year) {
        Map<String, Double> monthly = getMonthlyBreakdown(year);
        Map<String, Double> cumulative = new LinkedHashMap<>();

        double runningTotal = 0.0;
        for (Map.Entry<String, Double> entry : monthly.entrySet()) {
            runningTotal += entry.getValue();
            cumulative.put(entry.getKey(), runningTotal);
        }
        return cumulative;
    }

    /**
     * Returns a list of all distinct years found in recorded expenses.
     */
    public List<Integer> getAvailableYears() {
        List<Expense> allExpenses = expenseDAO.findAll();
        Set<Integer> years = allExpenses.stream()
                .map(e -> e.getDate().getYear())
                .collect(Collectors.toCollection(TreeSet::new));

        int currentYear = LocalDate.now().getYear();
        years.add(currentYear);

        // Return sorted in descending order (e.g., 2026, 2025, 2024)
        List<Integer> sorted = new ArrayList<>(years);
        sorted.sort(Collections.reverseOrder());
        return sorted;
    }

    /**
     * Retrieves the N most recent expenses for dashboard display.
     */
    public List<Expense> getRecentExpenses(int limit) {
        return expenseDAO.findRecent(limit);
    }
}
