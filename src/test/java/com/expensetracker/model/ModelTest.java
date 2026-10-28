package com.expensetracker.model;

import com.expensetracker.exception.InvalidAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Object-Oriented contracts (Inheritance, Polymorphism,
 * Encapsulation, Composition) across domain model classes.
 */
public class ModelTest {

    @Test
    @DisplayName("Expense should extend Transaction and implement polymorphic getDetails()")
    void testExpenseInheritanceAndPolymorphism() {
        Category foodCat = new Category(1, "Food", CategoryType.EXPENSE);
        PaymentMethod upi = new UpiPayment();
        LocalDate today = LocalDate.now();

        // Instantiate Expense (Subclass of Transaction)
        Transaction tx = new Expense(10, 450.00, foodCat, today, upi, "Lunch");

        // Verify Inheritance
        assertEquals(10, tx.getId());
        assertEquals(450.00, tx.getAmount());
        assertEquals(today, tx.getDate());

        // Verify Polymorphism: dynamically calls Expense.getDetails()
        String details = tx.getDetails();
        assertTrue(details.contains("Food"));
        assertTrue(details.contains("UPI"));
        assertTrue(details.contains("450.00"));
    }

    @Test
    @DisplayName("Transaction should enforce encapsulation and reject non-positive amounts")
    void testTransactionEncapsulation() {
        Category cat = new Category("Transport", CategoryType.EXPENSE);

        assertThrows(InvalidAmountException.class, () ->
                new Expense(-100, cat, LocalDate.now(), new CashPayment(), "Taxi")
        );
    }

    @Test
    @DisplayName("PaymentMethod polymorphic implementations should provide correct icons and types")
    void testPaymentMethodPolymorphism() {
        PaymentMethod cash = new CashPayment();
        PaymentMethod upi = new UpiPayment();
        PaymentMethod card = new CardPayment();

        assertEquals(PaymentType.CASH, cash.getType());
        assertEquals("💵", cash.getIcon());

        assertEquals(PaymentType.UPI, upi.getType());
        assertEquals("📱", upi.getIcon());

        assertEquals(PaymentType.CARD, card.getType());
        assertEquals("💳", card.getIcon());
    }

    @Test
    @DisplayName("Category setter should validate non-empty names")
    void testCategoryValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                new Category("", CategoryType.EXPENSE)
        );
    }

    @Test
    @DisplayName("Budget should validate monthly limit and month range (1 to 12)")
    void testBudgetValidation() {
        Category cat = new Category(1, "Utilities", CategoryType.EXPENSE);

        // Invalid limit
        assertThrows(InvalidAmountException.class, () ->
                new Budget(cat, -500, 5, 2026)
        );

        // Invalid month
        assertThrows(IllegalArgumentException.class, () ->
                new Budget(cat, 500, 13, 2026)
        );
    }

    @Test
    @DisplayName("BudgetProgress DTO should compute correct percentages and status flags")
    void testBudgetProgressCalculations() {
        Category cat = new Category(1, "Shopping", CategoryType.EXPENSE);
        Budget budget = new Budget(1, cat, 2000.00, 10, 2026);

        // Scenario 1: Spending within budget (1500 / 2000 = 75%)
        BudgetProgress bp1 = new BudgetProgress(budget, 1500.00);
        assertEquals(500.00, bp1.getRemaining(), 0.001);
        assertEquals(75.0, bp1.getPercentage(), 0.001);
        assertFalse(bp1.isExceeded());
        assertFalse(bp1.isNearLimit());

        // Scenario 2: Spending near limit (1800 / 2000 = 90%)
        BudgetProgress bp2 = new BudgetProgress(budget, 1800.00);
        assertTrue(bp2.isNearLimit());
        assertFalse(bp2.isExceeded());

        // Scenario 3: Spending exceeded (2500 / 2000 = 125%)
        BudgetProgress bp3 = new BudgetProgress(budget, 2500.00);
        assertTrue(bp3.isExceeded());
        assertEquals(500.00, bp3.getExceededAmount(), 0.001);
        assertEquals(0.0, bp3.getRemaining(), 0.001);
    }
}
