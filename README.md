# Expense Tracker & Budget Planner

A modern desktop Personal Finance & Analytics application built with **JavaFX** and **MySQL**, adhering to object-oriented programming (OOP) principles and clean architecture (MVC, DAO, and Service patterns).

![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-FF8000?style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

---

## 🚀 Features

- **Dashboard**: Real-time spending overview, monthly summary cards, and recent transactions.
- **Add Expense**: Record transactions with Indian Rupee (₹) amounts, categorized spending, date picker, polymorphic payment types (**Cash**, **UPI**, **Card**), and optional notes.
- **Expenses History**: Searchable and filterable data table with date range picker, category filter, in-place edit dialog, and delete confirmation.
- **Analytics & Reports**: Visual spending breakdown and insights across categories.
- **Budget Tracking**: Set monthly budgets per category with real-time budget threshold warnings when limits are exceeded.
- **Dark Theme UI**: Clean, responsive modern dark UI with custom CSS.

---

## 🛠️ Architecture & OOP Concepts

- **MVC (Model-View-Controller)**: Strict separation of concerns between FXML views, controller event handlers, and data models.
- **DAO Pattern (Data Access Object)**: Decoupled database operations (`ExpenseDAO`, `CategoryDAO`, `BudgetDAO`).
- **Service Layer**: Business rules and budget validation isolated in `ExpenseService`, `BudgetService`, and `AnalyticsService`.
- **Polymorphism & Abstraction**: Interface `PaymentMethod` implemented polymorphically by `CashPayment`, `UpiPayment`, and `CardPayment`.
- **Singleton Pattern**: Thread-safe `DatabaseConnection` singleton for connection management.
- **Defensive Programming & Validation**: Centralized input sanitation and validation in `Validator`.

---

## 📋 Prerequisites

- **Java JDK 21+** (e.g. Eclipse Adoptium Temurin or Oracle JDK)
- **Apache Maven 3.8+**
- **MySQL Server 8.0+**

---

## ⚙️ Setup & Installation

### 1. Clone the repository
```bash
git clone https://github.com/<YOUR-USERNAME>/expense-tracker.git
cd expense-tracker
```

### 2. Configure Database Credentials
Copy the example configuration to `config.properties`:
```bash
cp config.properties.example config.properties
```
Edit `config.properties` and enter your MySQL credentials:
```properties
db.url=jdbc:mysql://localhost:3306/expense_tracker_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
db.user=expense_user
db.password=password123
```

### 3. Initialize the Database
Execute `database.sql` into your MySQL server to create the database schema, tables, and seed categories:
```bash
mysql -u root -p < database.sql
```
*(On Windows, you can also run `setup-database.bat`)*

---

## ▶️ Running the Application

### Via Maven
```bash
mvn clean compile javafx:run
```

### Via Windows Batch Script
Double-click `run.bat` or run:
```cmd
run.bat
```

### Running Unit Tests
```bash
mvn test
```

---

## 🔒 Security
Sensitive database credentials in `config.properties` are ignored via `.gitignore` to prevent credentials from being exposed in version control.
