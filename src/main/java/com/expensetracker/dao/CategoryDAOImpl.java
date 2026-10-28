package com.expensetracker.dao;

import com.expensetracker.exception.DatabaseException;
import com.expensetracker.model.Category;
import com.expensetracker.model.CategoryType;
import com.expensetracker.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CategoryDAO.
 * 
 * OOP Concept: POLYMORPHISM (Interface implementation) & ENCAPSULATION
 * Why this class exists:
 * Encapsulates all SQL statements for the 'categories' table.
 * Uses try-with-resources to guarantee that connections, statements,
 * and result sets are automatically closed, preventing memory and connection leaks.
 */
public class CategoryDAOImpl implements CategoryDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public Category save(Category category) {
        String sql = "INSERT INTO categories (name, type) VALUES (?, ?)";

        // try-with-resources automatically closes Connection and PreparedStatement
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getType().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating category failed, no ID obtained.");
                }
            }
            return category;

        } catch (SQLException e) {
            throw new DatabaseException("Database error saving category: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Category> findById(int id) {
        String sql = "SELECT id, name, type FROM categories WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Database error finding category by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Category> findByName(String name) {
        String sql = "SELECT id, name, type FROM categories WHERE LOWER(name) = LOWER(?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Database error finding category by name: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT id, name, type FROM categories ORDER BY name ASC";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            return categories;

        } catch (SQLException e) {
            throw new DatabaseException("Database error fetching all categories: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, type = ? WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getType().name());
            stmt.setInt(3, category.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Database error updating category: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Database error deleting category: " + e.getMessage(), e);
        }
    }

    // Helper method to encapsulate ResultSet mapping logic
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String typeStr = rs.getString("type");
        CategoryType type = CategoryType.valueOf(typeStr.toUpperCase());
        return new Category(id, name, type);
    }
}
