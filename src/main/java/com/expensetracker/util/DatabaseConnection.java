package com.expensetracker.util;

import com.expensetracker.exception.DatabaseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connectivity using the SINGLETON PATTERN.
 * 
 * OOP Concept: SINGLETON PATTERN & ENCAPSULATION
 * Why this class exists:
 * 1. Single Point of Configuration: Database connection parameters (URL, user,
 *    and password read from 'config.properties') are loaded exactly once into memory.
 * 2. Information Hiding: No other class needs to know database URLs or passwords.
 *    They simply request a connection via DatabaseConnection.getInstance().getConnection().
 * 3. Thread-safe Lazy Initialization: Uses double-checked locking to ensure only
 *    one instance of this manager ever exists across the JVM lifecycle.
 */
public class DatabaseConnection {

    // Volatile instance for safe double-checked locking in multi-threaded environments
    private static volatile DatabaseConnection instance;

    private String url;
    private String user;
    private String password;

    /**
     * Private constructor prevents direct instantiation with 'new' from outside.
     * Enforces the Singleton pattern.
     */
    private DatabaseConnection() {
        loadConfiguration();
        testDriverAvailability();
    }

    /**
     * Global access point to retrieve the single instance of DatabaseConnection.
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Loads database credentials securely from config.properties.
     * If not found in the working directory, it checks the classpath resources.
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        File configFile = new File("config.properties");

        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                props.load(input);
            } catch (IOException e) {
                throw new DatabaseException("Failed to read external 'config.properties': " + e.getMessage(), e);
            }
        } else {
            // Fallback: look in classpath resources
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                if (input != null) {
                    props.load(input);
                } else {
                    throw new DatabaseException(
                        "Configuration file 'config.properties' was not found!\n" +
                        "Please copy 'config.properties.example' to 'config.properties' and set your MySQL credentials."
                    );
                }
            } catch (IOException e) {
                throw new DatabaseException("Error reading classpath 'config.properties': " + e.getMessage(), e);
            }
        }

        this.url = props.getProperty("db.url");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");

        if (this.url == null || this.user == null || this.password == null) {
            throw new DatabaseException(
                "Incomplete database configuration in 'config.properties'. " +
                "Ensure db.url, db.user, and db.password are all specified."
            );
        }
    }

    /**
     * Verifies that the MySQL JDBC Driver is present on the runtime classpath.
     */
    private void testDriverAvailability() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("MySQL JDBC Driver ('com.mysql.cj.jdbc.Driver') not found on classpath.", e);
        }
    }

    /**
     * Returns a new active Connection to the database.
     * 
     * Why return a new connection:
     * When DAOs use 'try-with-resources', the connection is automatically closed
     * upon leaving the block. Providing connections on-demand prevents "Connection is closed"
     * exceptions and avoids thread contention.
     *
     * @return an open java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.url, this.user, this.password);
    }

    /**
     * Convenience method to verify if a connection to MySQL succeeds.
     *
     * @return true if connection can be established, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
