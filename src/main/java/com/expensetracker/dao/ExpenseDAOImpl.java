package com.expensetracker.dao;

import com.expensetracker.exception.DatabaseException;
import com.expensetracker.model.Category;
import com.expensetracker.model.CategoryType;
import com.expensetracker.model.Expense;
import com.expensetracker.model.PaymentMethod;
import com.expensetracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ExpenseDAO.
 * 
 * OOP Concepts Demonstrated:
 * 1. POLYMORPHISM: Implements ExpenseDAO interface.
 * 2. COMPOSITION HYDRATION: Reconstructs Expense objects containing Category
 *    and polymorphic PaymentMethod (CashPayment, UpiPayment, CardPayment) from SQL JOINs.
 * 3. TRY-WITH-RESOURCES: Guarantees JDBC resources are automatically freed.
 */
public class ExpenseDAOImpl implements ExpenseDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String BASE_SELECT =
            "SELECT e.id, e.amount, e.expense_date, e.payment_method, e.note, " +
            "       c.id AS category_id, c.name AS category_name, c.type AS category_type " +
            "FROM expenses e " +
            "JOIN categories c ON e.category_id = c.id ";

    @Override
    public Expense save(Expense expense) {
        String sql = "INSERT INTO expenses (amount, category_id, expense_date, payment_method, note) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDouble(1, expense.getAmount());
            stmt.setInt(2, expense.getCategory().getId());
            stmt.setDate(3, Date.valueOf(expense.getDate()));
            stmt.setString(4, expense.getPaymentMethod().getType().name());
            stmt.setString(5, expense.getNote());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Failed to save expense, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    expense.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Failed to save expense, no ID obtained.");
                }
            }
            return expense;

        } catch (SQLException e) {
            throw new DatabaseException("Database error saving expense: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Expense> findById(int id) {
        String sql = BASE_SELECT + "WHERE e.id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToExpense(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Database error finding expense by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Expense> findAll() {
        String sql = BASE_SELECT + "ORDER BY e.expense_date DESC, e.id DESC";
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                expenses.add(mapResultSetToExpense(rs));
            }
            return expenses;

        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving all expenses: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Expense expense) {
        String sql = "UPDATE expenses SET amount = ?, category_id = ?, expense_date = ?, " +
                     "payment_method = ?, note = ? WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, expense.getAmount());
            stmt.setInt(2, expense.getCategory().getId());
            stmt.setDate(3, Date.valueOf(expense.getDate()));
            stmt.setString(4, expense.getPaymentMethod().getType().name());
            stmt.setString(5, expense.getNote());
            stmt.setInt(6, expense.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Database error updating expense: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Database error deleting expense: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Expense> findByCategory(int categoryId) {
        String sql = BASE_SELECT + "WHERE e.category_id = ? ORDER BY e.expense_date DESC";
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapResultSetToExpense(rs));
                }
            }
            return expenses;

        } catch (SQLException e) {
            throw new DatabaseException("Database error fetching expenses by category: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Expense> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = BASE_SELECT + "WHERE e.expense_date BETWEEN ? AND ? ORDER BY e.expense_date DESC";
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapResultSetToExpense(rs));
                }
            }
            return expenses;

        } catch (SQLException e) {
            throw new DatabaseException("Database error fetching expenses by date range: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Expense> search(String keyword, Integer categoryId, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND LOWER(e.note) LIKE ? ");
            params.add("%" + keyword.trim().toLowerCase() + "%");
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND e.category_id = ? ");
            params.add(categoryId);
        }
        if (startDate != null) {
            sql.append("AND e.expense_date >= ? ");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append("AND e.expense_date <= ? ");
            params.add(Date.valueOf(endDate));
        }

        sql.append("ORDER BY e.expense_date DESC, e.id DESC");
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapResultSetToExpense(rs));
                }
            }
            return expenses;

        } catch (SQLException e) {
            throw new DatabaseException("Database error searching expenses: " + e.getMessage(), e);
        }
    }

    @Override
    public double getTotalExpensesByMonth(int month, int year) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses " +
                     "WHERE MONTH(expense_date) = ? AND YEAR(expense_date) = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, month);
            stmt.setInt(2, year);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
            return 0.0;

        } catch (SQLException e) {
            throw new DatabaseException("Database error computing monthly total: " + e.getMessage(), e);
        }
    }

    @Override
    public double getTotalExpensesAllTime() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;

        } catch (SQLException e) {
            throw new DatabaseException("Database error computing all-time total: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Expense> findRecent(int limit) {
        String sql = BASE_SELECT + "ORDER BY e.expense_date DESC, e.id DESC LIMIT ?";
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapResultSetToExpense(rs));
                }
            }
            return expenses;

        } catch (SQLException e) {
            throw new DatabaseException("Database error fetching recent expenses: " + e.getMessage(), e);
        }
    }

    // Maps a joined SQL row into a fully-hydrated Expense object
    private Expense mapResultSetToExpense(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        double amount = rs.getDouble("amount");
        LocalDate date = rs.getDate("expense_date").toLocalDate();
        String pmString = rs.getString("payment_method");
        String note = rs.getString("note");

        // Hydrate Category object
        int catId = rs.getInt("category_id");
        String catName = rs.getString("category_name");
        CategoryType catType = CategoryType.valueOf(rs.getString("category_type").toUpperCase());
        Category category = new Category(catId, catName, catType);

        // Hydrate polymorphic PaymentMethod via Factory helper
        PaymentMethod paymentMethod = PaymentMethod.fromString(pmString);

        return new Expense(id, amount, category, date, paymentMethod, note);
    }
}
