package com.expensetracker.exception;

/**
 * Custom runtime exception for database operations.
 * 
 * OOP Concept: ABSTRACTION & EXCEPTION SHIELDING
 * Why this class exists:
 * Low-level JDBC code throws java.sql.SQLException, which exposes raw database
 * error codes and query details. By catching SQLException and wrapping it into
 * a custom DatabaseException, we shield higher layers (Service, Controller, UI)
 * from direct database coupling and provide clean, friendly error messages.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
