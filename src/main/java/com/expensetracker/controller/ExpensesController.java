package com.expensetracker.controller;

import com.expensetracker.exception.DatabaseException;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.util.CurrencyFormatter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Expenses History Table view.
 * 
 * OOP Concepts Demonstrated:
 * 1. COLLECTIONS & GENERICS: Uses ObservableList<Expense>, List<Category>, and
 *    Generic TableColumn<Expense, T> bindings.
 * 2. FUNCTIONAL PROGRAMMING & STREAMS: Uses Java Streams for real-time aggregation
 *    (filtering, summing amounts).
 * 3. EVENT-DRIVEN PROGRAMMING: Table row selection listeners, double-click to edit,
 *    live search textProperty listeners.
 */
public class ExpensesController {

    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<Category> comboCategoryFilter;

    @FXML
    private DatePicker dpFromDate;

    @FXML
    private DatePicker dpToDate;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private TableView<Expense> tableExpenses;

    @FXML
    private TableColumn<Expense, Integer> colId;

    @FXML
    private TableColumn<Expense, String> colAmount;

    @FXML
    private TableColumn<Expense, String> colCategory;

    @FXML
    private TableColumn<Expense, LocalDate> colDate;

    @FXML
    private TableColumn<Expense, String> colPaymentMethod;

    @FXML
    private TableColumn<Expense, String> colNote;

    @FXML
    private Label lblSummary;

    private final ExpenseService expenseService = new ExpenseService();
    private final ObservableList<Expense> expenseList = FXCollections.observableArrayList();
    private List<Category> allCategories;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSelectionListeners();
        setupSearchListeners();
        loadCategoriesAndExpenses();
    }

    /**
     * Binds TableView columns to Expense model properties.
     * Demonstrates Encapsulation and Property Value extraction.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Format amount column using CurrencyFormatter
        colAmount.setCellValueFactory(cellData ->
                new SimpleStringProperty(CurrencyFormatter.format(cellData.getValue().getAmount())));

        // Extract Category name via Composition
        colCategory.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategoryName()));

        // Extract Payment Method with icon via Polymorphism
        colPaymentMethod.setCellValueFactory(cellData -> {
            Expense e = cellData.getValue();
            String icon = e.getPaymentMethod() != null ? e.getPaymentMethod().getIcon() + " " : "";
            return new SimpleStringProperty(icon + e.getPaymentMethodName());
        });

        tableExpenses.setItems(expenseList);
    }

    /**
     * Enables or disables Edit/Delete buttons based on row selection.
     */
    private void setupSelectionListeners() {
        tableExpenses.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            btnEdit.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection);
        });

        // Double-click to edit row
        tableExpenses.setRowFactory(tv -> {
            TableRow<Expense> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleEditExpense(null);
                }
            });
            return row;
        });
    }

    /**
     * Attaches live filtering to search inputs.
     */
    private void setupSearchListeners() {
        // Live search on typing
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Category filter change
        comboCategoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Date picker filter changes
        dpFromDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dpToDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Loads category options and fetches all expenses from the database.
     */
    private void loadCategoriesAndExpenses() {
        new Thread(() -> {
            try {
                allCategories = expenseService.getAllCategories();
                List<Expense> expenses = expenseService.getAllExpenses();

                Platform.runLater(() -> {
                    // Populate category filter dropdown with "All Categories" option
                    Category allOption = new Category(0, "All Categories", null);
                    ObservableList<Category> filterOptions = FXCollections.observableArrayList();
                    filterOptions.add(allOption);
                    filterOptions.addAll(allCategories);
                    comboCategoryFilter.setItems(filterOptions);
                    comboCategoryFilter.getSelectionModel().selectFirst();

                    expenseList.setAll(expenses);
                    updateSummary(expenses);
                });

            } catch (DatabaseException e) {
                Platform.runLater(() -> showErrorAlert("Database Error", "Failed to load data: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Queries database with active search text, category filter, and date range.
     */
    @FXML
    private void applyFilters() {
        String keyword = txtSearch.getText();
        Category selectedCategory = comboCategoryFilter.getValue();
        Integer categoryId = (selectedCategory != null && selectedCategory.getId() > 0) ? selectedCategory.getId() : null;
        LocalDate startDate = dpFromDate.getValue();
        LocalDate endDate = dpToDate.getValue();

        new Thread(() -> {
            try {
                List<Expense> filtered = expenseService.searchExpenses(keyword, categoryId, startDate, endDate);
                Platform.runLater(() -> {
                    expenseList.setAll(filtered);
                    updateSummary(filtered);
                });
            } catch (DatabaseException e) {
                Platform.runLater(() -> showErrorAlert("Filter Error", e.getMessage()));
            }
        }).start();
    }

    /**
     * Resets all search and filter fields.
     */
    @FXML
    private void handleResetFilters(ActionEvent event) {
        txtSearch.clear();
        comboCategoryFilter.getSelectionModel().selectFirst();
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        applyFilters();
    }

    /**
     * Opens the Edit Expense modal dialog for the selected row.
     */
    @FXML
    private void handleEditExpense(ActionEvent event) {
        Expense selectedExpense = tableExpenses.getSelectionModel().getSelectedItem();
        if (selectedExpense == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/expensetracker/view/EditExpenseDialog.fxml"));
            Parent root = loader.load();

            EditExpenseDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Expense #" + selectedExpense.getId());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableExpenses.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);
            controller.setExpense(selectedExpense, allCategories);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                applyFilters(); // Refresh table
            }

        } catch (IOException e) {
            showErrorAlert("Error", "Could not open edit dialog: " + e.getMessage());
        }
    }

    /**
     * Prompts confirmation and deletes the selected expense.
     */
    @FXML
    private void handleDeleteExpense(ActionEvent event) {
        Expense selected = tableExpenses.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Expense #" + selected.getId() + "?");
        confirmAlert.setContentText(String.format("Are you sure you want to delete this expense?\n\nAmount: %s\nCategory: %s\nDate: %s",
                CurrencyFormatter.format(selected.getAmount()), selected.getCategoryName(), selected.getDate()));

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = expenseService.deleteExpense(selected.getId());
                if (deleted) {
                    applyFilters();
                }
            } catch (DatabaseException e) {
                showErrorAlert("Delete Failed", e.getMessage());
            }
        }
    }

    /**
     * Computes real-time totals using Java Streams.
     */
    private void updateSummary(List<Expense> currentExpenses) {
        double total = expenseService.calculateTotalAmount(currentExpenses);
        lblSummary.setText(String.format("Showing %d expense%s  •  Total: %s",
                currentExpenses.size(),
                currentExpenses.size() == 1 ? "" : "s",
                CurrencyFormatter.format(total)));
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
