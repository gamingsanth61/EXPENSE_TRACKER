package com.expensetracker.util;

import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.dao.CategoryDAOImpl;
import com.expensetracker.model.Category;

import java.sql.Connection;
import java.util.List;

/**
 * Quick command-line diagnostic tool to verify MySQL database setup.
 * Run this to check if your credentials in 'config.properties' and 'database.sql' tables work.
 */
public class DatabaseTest {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   EXPENSE TRACKER - DATABASE CONNECTION TEST    ");
        System.out.println("=================================================");

        try {
            System.out.print("[1/3] Reading config.properties and connecting to MySQL... ");
            try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                if (conn != null && !conn.isClosed()) {
                    System.out.println("SUCCESS! [CONNECTED]");
                }
            }

            System.out.print("[2/3] Querying categories table using CategoryDAO... ");
            CategoryDAO categoryDAO = new CategoryDAOImpl();
            List<Category> categories = categoryDAO.findAll();
            System.out.println("SUCCESS! Found " + categories.size() + " categories.");

            System.out.println("[3/3] Category listing:");
            for (Category cat : categories) {
                System.out.printf("      • #%d: %s (%s)%n", cat.getId(), cat.getName(), cat.getType());
            }

            System.out.println("=================================================");
            System.out.println("All checks PASSED! Your database is ready.");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("\n[FAILED] Could not connect to MySQL.");
            System.err.println("Error details: " + e.getMessage());
            System.err.println("\nTroubleshooting tips:");
            System.err.println("1. Is MySQL server running on localhost:3306?");
            System.err.println("2. Did you execute 'database.sql' in MySQL?");
            System.err.println("3. Are 'db.user' and 'db.password' correct in 'config.properties'?");
        }
    }
}
