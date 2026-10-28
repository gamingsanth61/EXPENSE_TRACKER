package com.expensetracker.controller;

import com.expensetracker.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the Main Application Layout (Sidebar and Content Area).
 * 
 * OOP Concept: CONTROLLER IN MVC PATTERN & SINGLE RESPONSIBILITY PRINCIPLE
 * Why this class exists:
 * The MainController coordinates user navigation events and view swapping.
 * It does not contain direct database queries or business calculations; it delegates
 * to individual page controllers and services.
 */
public class MainController {

    private static MainController instance;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnAddExpense;

    @FXML
    private Button btnExpenses;

    @FXML
    private Button btnAnalytics;

    @FXML
    private Button btnBudget;

    @FXML
    private Label lblPageTitle;

    @FXML
    private Label lblDbStatus;

    @FXML
    private StackPane contentPane;

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;

        // Load initial Dashboard view
        navigateTo("Dashboard", "/com/expensetracker/view/DashboardView.fxml", btnDashboard);

        // Check MySQL connection in background to keep UI snappy
        checkDatabaseStatusAsync();
    }

    public void showAddExpense() {
        handleNavAddExpense(null);
    }

    public void showDashboard() {
        handleNavDashboard(null);
    }

    @FXML
    private void handleNavDashboard(ActionEvent event) {
        navigateTo("Dashboard", "/com/expensetracker/view/DashboardView.fxml", btnDashboard);
    }

    @FXML
    private void handleNavAddExpense(ActionEvent event) {
        navigateTo("Add Expense", "/com/expensetracker/view/AddExpenseView.fxml", btnAddExpense);
    }

    @FXML
    private void handleNavExpenses(ActionEvent event) {
        navigateTo("Expense History", "/com/expensetracker/view/ExpensesView.fxml", btnExpenses);
    }

    @FXML
    private void handleNavAnalytics(ActionEvent event) {
        navigateTo("Spending Analytics", "/com/expensetracker/view/AnalyticsView.fxml", btnAnalytics);
    }

    @FXML
    private void handleNavBudget(ActionEvent event) {
        navigateTo("Budget Management", "/com/expensetracker/view/BudgetView.fxml", btnBudget);
    }

    /**
     * Loads and displays the requested sub-view in the central content pane.
     * We load views freshly on navigation to ensure real-time data synchronization.
     */
    private void navigateTo(String title, String fxmlPath, Button activeButton) {
        lblPageTitle.setText(title);
        highlightActiveButton(activeButton);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node viewNode = loader.load();
            contentPane.getChildren().setAll(viewNode);

        } catch (IOException e) {
            lblPageTitle.setText("Error loading view");
            Label errLabel = new Label("Could not load view: " + fxmlPath + "\n" + e.getMessage());
            errLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;");
            contentPane.getChildren().setAll(errLabel);
        }
    }

    /**
     * Updates sidebar button styling so the user clearly sees which tab is active.
     */
    private void highlightActiveButton(Button activeButton) {
        Button[] buttons = {btnDashboard, btnAddExpense, btnExpenses, btnAnalytics, btnBudget};
        for (Button btn : buttons) {
            btn.getStyleClass().remove("nav-btn-active");
        }
        if (!activeButton.getStyleClass().contains("nav-btn-active")) {
            activeButton.getStyleClass().add("nav-btn-active");
        }
    }

    /**
     * Checks database connectivity in a separate thread so UI does not freeze on startup.
     */
    private void checkDatabaseStatusAsync() {
        new Thread(() -> {
            boolean connected = DatabaseConnection.getInstance().testConnection();
            Platform.runLater(() -> {
                if (connected) {
                    lblDbStatus.setText("● MySQL Connected (expense_user)");
                    lblDbStatus.getStyleClass().clear();
                    lblDbStatus.getStyleClass().add("status-badge");
                } else {
                    lblDbStatus.setText("● MySQL Disconnected (Check config.properties)");
                    lblDbStatus.getStyleClass().clear();
                    lblDbStatus.getStyleClass().add("status-badge-error");
                }
            });
        }).start();
    }
}
