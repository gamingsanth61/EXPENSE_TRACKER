package com.expensetracker.model;

import java.util.Objects;

/**
 * Model class representing an Expense or Income Category.
 * 
 * OOP Concept: ENCAPSULATION
 * Why this class exists:
 * 1. Data Hiding: All fields ('id', 'name', 'type') are declared 'private'.
 * 2. Invariant Protection: The setter for 'name' validates that the category name
 *    cannot be null or blank, ensuring the object is always in a valid state.
 * 3. Domain Model: Represents a business entity mapped from the 'categories' database table.
 */
public class Category {

    private int id;
    private String name;
    private CategoryType type;

    // Default constructor (required by some frameworks / serializers)
    public Category() {
        this.type = CategoryType.EXPENSE;
    }

    // Parameterized constructor without ID (for newly created categories not yet saved to DB)
    public Category(String name, CategoryType type) {
        setName(name);
        this.type = (type != null) ? type : CategoryType.EXPENSE;
    }

    // Full constructor (for reconstructing categories loaded from the database)
    public Category(int id, String name, CategoryType type) {
        this.id = id;
        setName(name);
        this.type = (type != null) ? type : CategoryType.EXPENSE;
    }

    // Getters and Setters with validation
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty or null.");
        }
        this.name = name.trim();
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = (type != null) ? type : CategoryType.EXPENSE;
    }

    // Overriding toString() for convenient display in JavaFX ComboBox and logs
    @Override
    public String toString() {
        return name;
    }

    // Overriding equals() and hashCode() so categories can be compared by ID and used in Maps/Sets
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id || Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
