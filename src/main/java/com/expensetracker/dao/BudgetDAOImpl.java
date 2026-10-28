package com.expensetracker.dao;

import com.expensetracker.exception.DatabaseException;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.CategoryType;
import com.expensetracker.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of BudgetDAO.
 * 
 * OOP Concept: POLYMORPHISM (Interface implementation) & TRY-WITH-RESOURCES
 */
public class BudgetDAOImpl implements BudgetDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String BASE_SELECT =
            "SELECT b.id, b.monthly_limit, b.month, b.year, " +
            "       c.id AS category_id, c.name AS category_name, c.type AS category_type " +
            "FROM budgets b " +
            "JOIN categories c ON b.category_id = c.id ";

    @Override
    public Budget save(Budget budget) {
        // Upsert style: insert or update if already exists for category, month, and year
        String sql = "INSERT INTO budgets (category_id, monthly_limit, month, year) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE monthly_limit = VALUES(monthly_limit)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, budget.getCategory().getId());
            stmt.setDouble(2, budget.getMonthlyLimit());
            stmt.setInt(3, budget.getMonth());
            stmt.setInt(4, budget.getYear());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    budget.setId(generatedKeys.getInt(1));
                }
            }
            return budget;

        } catch (SQLException e) {
            throw new DatabaseException("Database error saving budget: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Budget> findById(int id) {
        String sql = BASE_SELECT + "WHERE b.id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBudget(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Database error finding budget by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Budget> findAll() {
        String sql = BASE_SELECT + "ORDER BY b.year DESC, b.month DESC, c.name ASC";
        List<Budget> list = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToBudget(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving all budgets: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Budget budget) {
        String sql = "UPDATE budgets SET category_id = ?, monthly_limit = ?, month = ?, year = ? WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, budget.getCategory().getId());
            stmt.setDouble(2, budget.getMonthlyLimit());
            stmt.setInt(3, budget.getMonth());
            stmt.setInt(4, budget.getYear());
            stmt.setInt(5, budget.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Database error updating budget: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM budgets WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Database error deleting budget: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Budget> findByCategoryAndMonthYear(int categoryId, int month, int year) {
        String sql = BASE_SELECT + "WHERE b.category_id = ? AND b.month = ? AND b.year = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBudget(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Database error fetching budget for category and month: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Budget> findByMonthYear(int month, int year) {
        String sql = BASE_SELECT + "WHERE b.month = ? AND b.year = ? ORDER BY c.name ASC";
        List<Budget> list = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, month);
            stmt.setInt(2, year);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBudget(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DatabaseException("Database error fetching budgets by month/year: " + e.getMessage(), e);
        }
    }

    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        double limit = rs.getDouble("monthly_limit");
        int month = rs.getInt("month");
        int year = rs.getInt("year");

        int catId = rs.getInt("category_id");
        String catName = rs.getString("category_name");
        CategoryType catType = CategoryType.valueOf(rs.getString("category_type").toUpperCase());
        Category category = new Category(catId, catName, catType);

        return new Budget(id, category, limit, month, year);
    }
}
