package com.expensetracker.controller;

import com.expensetracker.exception.BudgetExceededException;
import com.expensetracker.exception.DatabaseException;
import com.expensetracker.exception.InvalidAmountException;
import com.expensetracker.model.*;
import com.expensetracker.service.ExpenseService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the 'Add Expense' form view.
 * 
 * OOP Concept: MVC CONTROLLER & POLYMORPHIC UI BINDING
 * Why this class exists:
 * 1. Handles UI events when the user clicks 'Save Expense' or 'Reset'.
 * 2. Bridges UI inputs (TextField, DatePicker, ComboBox, RadioButtons) to the Service Layer.
 * 3. Demonstrates Polymorphism: Maps the user's selected RadioButton to a concrete
 *    PaymentMethod instance (CashPayment, UpiPayment, CardPayment) through the common
 *    PaymentMethod interface.
 */
public class AddExpenseController {

    @FXML
    private TextField txtAmount;

    @FXML
    private ComboBox<Category> comboCategory;

    @FXML
    private DatePicker dpDate;

    @FXML
    private ToggleGroup tgPaymentMethod;

    @FXML
    private RadioButton rbCash;

    @FXML
    private RadioButton rbUpi;

    @FXML
    private RadioButton rbCard;

    @FXML
    private TextField txtNote;

    @FXML
    private Label lblFeedback;

    private final ExpenseService expenseService = new ExpenseService();

    @FXML
    public void initialize() {
        // Initialize default form values
        dpDate.setValue(LocalDate.now());
        rbCash.setSelected(true);
        hideFeedback();

        // Populate Category ComboBox from database in background
        loadCategories();
    }

    /**
     * Loads available categories from the MySQL database and binds them to the ComboBox.
     */
    public void loadCategories() {
        new Thread(() -> {
            try {
                List<Category> categories = expenseService.getAllCategories();
                Platform.runLater(() -> {
                    comboCategory.setItems(FXCollections.observableArrayList(categories));
                    if (!categories.isEmpty()) {
                        comboCategory.getSelectionModel().selectFirst();
                    }
                });
            } catch (DatabaseException e) {
                Platform.runLater(() -> showFeedback("Database error: Could not load categories. Ensure MySQL is running.", FeedbackType.ERROR));
            }
        }).start();
    }

    /**
     * Handles saving the new expense when user clicks 'Save Expense'.
     */
    @FXML
    private void handleSaveExpense(ActionEvent event) {
        hideFeedback();

        String rawAmount = txtAmount.getText();
        Category selectedCategory = comboCategory.getValue();
        LocalDate selectedDate = dpDate.getValue();
        String note = txtNote.getText();

        // Polymorphic selection of PaymentMethod based on chosen RadioButton
        PaymentMethod paymentMethod = getSelectedPaymentMethod();

        try {
            Expense savedExpense = expenseService.addExpense(
                    rawAmount, selectedCategory, selectedDate, paymentMethod, note
            );

            // Successfully saved
            showFeedback("✓ Expense recorded successfully: " + savedExpense.getDetails(), FeedbackType.SUCCESS);
            clearInputsExceptDate();

        } catch (BudgetExceededException e) {
            // Business rule warning: Expense was saved, but crosses the monthly budget!
            showFeedback("⚠️ " + e.getMessage(), FeedbackType.WARNING);
            clearInputsExceptDate();

        } catch (IllegalArgumentException e) {
            showFeedback("✕ Validation error: " + e.getMessage(), FeedbackType.ERROR);

        } catch (DatabaseException e) {
            showFeedback("✕ Database failure: " + e.getMessage(), FeedbackType.ERROR);
        }
    }

    /**
     * Resets all input fields back to defaults.
     */
    @FXML
    private void handleReset(ActionEvent event) {
        clearAllInputs();
        hideFeedback();
    }

    /**
     * Polymorphism in action:
     * Returns an instance of PaymentMethod without callers needing to know
     * the concrete class details.
     */
    private PaymentMethod getSelectedPaymentMethod() {
        if (rbUpi.isSelected()) {
            return new UpiPayment();
        } else if (rbCard.isSelected()) {
            return new CardPayment();
        }
        return new CashPayment();
    }

    private void clearInputsExceptDate() {
        txtAmount.clear();
        txtNote.clear();
        txtAmount.requestFocus();
    }

    private void clearAllInputs() {
        txtAmount.clear();
        txtNote.clear();
        dpDate.setValue(LocalDate.now());
        rbCash.setSelected(true);
        if (!comboCategory.getItems().isEmpty()) {
            comboCategory.getSelectionModel().selectFirst();
        }
        txtAmount.requestFocus();
    }

    private void showFeedback(String message, FeedbackType type) {
        lblFeedback.setText(message);
        lblFeedback.setVisible(true);
        lblFeedback.setManaged(true);
        lblFeedback.getStyleClass().removeAll("feedback-success", "feedback-error", "feedback-warning");

        switch (type) {
            case SUCCESS -> lblFeedback.getStyleClass().add("feedback-success");
            case ERROR -> lblFeedback.getStyleClass().add("feedback-error");
            case WARNING -> lblFeedback.getStyleClass().add("feedback-warning");
        }
    }

    private void hideFeedback() {
        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);
    }

    private enum FeedbackType {
        SUCCESS, ERROR, WARNING
    }
}
