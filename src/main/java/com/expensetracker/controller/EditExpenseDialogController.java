package com.expensetracker.controller;

import com.expensetracker.exception.BudgetExceededException;
import com.expensetracker.exception.DatabaseException;
import com.expensetracker.model.*;
import com.expensetracker.service.ExpenseService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the Edit Expense Modal Dialog.
 * 
 * OOP Concept: ENCAPSULATION & DEFENSIVE PROGRAMMING
 * Why this class exists:
 * Encapsulates the state and workflow for modifying an existing Expense record.
 * Validates updated user inputs and synchronizes the change back to the database.
 */
public class EditExpenseDialogController {

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
    private Expense expenseToEdit;
    private boolean saveClicked = false;
    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    /**
     * Initializes the form with the selected expense's existing data.
     */
    public void setExpense(Expense expense, List<Category> categories) {
        this.expenseToEdit = expense;
        comboCategory.setItems(FXCollections.observableArrayList(categories));

        txtAmount.setText(String.format("%.2f", expense.getAmount()));
        comboCategory.setValue(expense.getCategory());
        dpDate.setValue(expense.getDate());
        txtNote.setText(expense.getNote());

        PaymentType type = expense.getPaymentMethod().getType();
        switch (type) {
            case CASH -> rbCash.setSelected(true);
            case UPI -> rbUpi.setSelected(true);
            case CARD -> rbCard.setSelected(true);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        String rawAmount = txtAmount.getText();
        Category selectedCategory = comboCategory.getValue();
        LocalDate selectedDate = dpDate.getValue();
        String note = txtNote.getText();

        PaymentMethod paymentMethod = rbUpi.isSelected() ? new UpiPayment() :
                (rbCard.isSelected() ? new CardPayment() : new CashPayment());

        try {
            expenseService.updateExpense(
                    expenseToEdit.getId(), rawAmount, selectedCategory, selectedDate, paymentMethod, note
            );
            saveClicked = true;
            dialogStage.close();

        } catch (BudgetExceededException e) {
            // Budget warning, but update still succeeded
            saveClicked = true;
            Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK);
            alert.setHeaderText("Budget Threshold Warning");
            alert.showAndWait();
            dialogStage.close();

        } catch (IllegalArgumentException e) {
            lblFeedback.setText("✕ " + e.getMessage());
            lblFeedback.setVisible(true);
            lblFeedback.setManaged(true);

        } catch (DatabaseException e) {
            lblFeedback.setText("✕ Database failure: " + e.getMessage());
            lblFeedback.setVisible(true);
            lblFeedback.setManaged(true);
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        dialogStage.close();
    }
}
