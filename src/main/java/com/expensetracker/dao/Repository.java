package com.expensetracker.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic base interface for Data Access Object (DAO) pattern.
 * 
 * OOP Concepts: ABSTRACTION, GENERICS & DAO PATTERN
 * Why this interface exists:
 * 1. Abstraction & Decoupling: Hides underlying database implementation details
 *    (SQL, JDBC, MySQL) from business logic. If we ever switch to PostgreSQL or an API,
 *    only the DAO implementation classes change, leaving the rest of the application intact.
 * 2. Generics (<T>): Enables compile-time type safety and code reusability across
 *    different domain models (Category, Expense, Budget).
 * 3. Contract: Mandates fundamental CRUD operations (Create, Read, Update, Delete).
 *
 * @param <T> The model entity type handled by the repository
 */
public interface Repository<T> {

    /**
     * Inserts a new entity into the database and sets its generated ID.
     *
     * @param entity the model object to save
     * @return the saved entity with generated ID populated
     */
    T save(T entity);

    /**
     * Retrieves an entity by its primary key ID.
     *
     * @param id the primary key
     * @return an Optional containing the entity if found, or empty Optional
     */
    Optional<T> findById(int id);

    /**
     * Retrieves all entities of type T from the database.
     *
     * @return List of all records
     */
    List<T> findAll();

    /**
     * Updates an existing entity in the database.
     *
     * @param entity the entity with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean update(T entity);

    /**
     * Deletes an entity by its primary key ID.
     *
     * @param id the primary key
     * @return true if deleted successfully, false otherwise
     */
    boolean delete(int id);
}
