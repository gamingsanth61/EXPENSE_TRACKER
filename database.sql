-- =====================================================================
-- Database Setup Script: Expense Tracker with Analytics
-- Run this script in MySQL Workbench or via MySQL Command Line Client:
--   mysql -u root -p < database.sql
-- =====================================================================

-- 1. Create the Database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS expense_tracker_db;
USE expense_tracker_db;

-- 2. Optional: Create user 'expense_user' and grant permissions
-- You can run these statements as the 'root' user:
-- CREATE USER IF NOT EXISTS 'expense_user'@'localhost' IDENTIFIED BY 'password123';
-- GRANT ALL PRIVILEGES ON expense_tracker_db.* TO 'expense_user'@'localhost';
-- FLUSH PRIVILEGES;

-- 3. Drop existing tables in reverse order of foreign keys (clean slate)
DROP TABLE IF EXISTS budgets;
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS categories;

-- 4. Table: categories
-- Represents spending categories (e.g., Food, Travel, Utilities)
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    type ENUM('EXPENSE', 'INCOME') NOT NULL DEFAULT 'EXPENSE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 5. Table: expenses
-- Represents individual expenditure transactions
CREATE TABLE expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(10, 2) NOT NULL,
    category_id INT NOT NULL,
    expense_date DATE NOT NULL,
    payment_method ENUM('CASH', 'UPI', 'CARD') NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expenses_category 
        FOREIGN KEY (category_id) REFERENCES categories(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Table: budgets
-- Monthly budget threshold per category
CREATE TABLE budgets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    monthly_limit DECIMAL(10, 2) NOT NULL,
    month INT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year INT NOT NULL CHECK (year >= 2000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_category_month_year UNIQUE (category_id, month, year),
    CONSTRAINT fk_budgets_category 
        FOREIGN KEY (category_id) REFERENCES categories(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Seed Data: Default Categories
INSERT INTO categories (name, type) VALUES
('Food & Dining', 'EXPENSE'),
('Groceries', 'EXPENSE'),
('Shopping', 'EXPENSE'),
('Transportation', 'EXPENSE'),
('Bills & Utilities', 'EXPENSE'),
('Entertainment', 'EXPENSE'),
('Health & Fitness', 'EXPENSE'),
('Education', 'EXPENSE'),
('Personal Care', 'EXPENSE'),
('Others', 'EXPENSE');

-- Verify insertion
SELECT * FROM categories;
