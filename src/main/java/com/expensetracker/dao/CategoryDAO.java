package com.expensetracker.dao;

import com.expensetracker.model.Category;

import java.util.Optional;

/**
 * Data Access Object interface for Category entities.
 * 
 * OOP Concept: INTERFACE INHERITANCE
 * Extends the generic Repository<Category> interface and adds
 * category-specific query contracts.
 */
public interface CategoryDAO extends Repository<Category> {

    /**
     * Looks up a category by its unique name.
     *
     * @param name Name of the category (e.g. "Food & Dining")
     * @return Optional containing the Category if present
     */
    Optional<Category> findByName(String name);
}
