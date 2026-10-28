package com.expensetracker.controller;

import com.expensetracker.exception.DatabaseException;
import com.expensetracker.model.Expense;
import com.expensetracker.service.AnalyticsService;
import com.expensetracker.util.CurrencyFormatter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the Dashboard Summary view.
 * 
 * OOP Concept: MVC CONTROLLER & SERVICE DELEGATION
 * Why this class exists:
 * Coordinates the presentation of high-level financial metrics (Total Spending,
 * Current Month Spending, Top Category, and Recent Expenses).
 * It delegates all mathematical and stream computations to AnalyticsService.
 */
public class DashboardController {

    @FXML
    private Label lblTotalSpending;

    @FXML
    private Label lblMonthSpending;

    @FXML
    private Label lblMonthName;

    @FXML
    private Label lblTopCategoryName;

    @FXML
    private Label lblTopCategoryAmount;

    @FXML
    private VBox boxRecentExpenses;

    @FXML
    private Label lblRecentEmpty;

    private final AnalyticsService analyticsService = new AnalyticsService();

    @FXML
    public void initialize() {
        lblMonthName.setText("Spending for " + LocalDate.now().getMonth().name().substring(0, 1) +
                LocalDate.now().getMonth().name().substring(1).toLowerCase() + " " + LocalDate.now().getYear());
        loadDashboardData();
    }

    /**
     * Loads metrics from the service asynchronously to ensure instantaneous UI response.
     */
    public void loadDashboardData() {
        new Thread(() -> {
            try {
                double totalAllTime = analyticsService.getTotalSpendingAllTime();
                double totalMonth = analyticsService.getCurrentMonthSpending();
                Optional<Map.Entry<String, Double>> topCategoryOpt = analyticsService.getTopSpendingCategory();
                List<Expense> recentExpenses = analyticsService.getRecentExpenses(5);

                Platform.runLater(() -> {
                    // Update metric cards
                    lblTotalSpending.setText(CurrencyFormatter.format(totalAllTime));
                    lblMonthSpending.setText(CurrencyFormatter.format(totalMonth));

                    if (topCategoryOpt.isPresent()) {
                        Map.Entry<String, Double> entry = topCategoryOpt.get();
                        lblTopCategoryName.setText(entry.getKey());
                        lblTopCategoryAmount.setText("Total: " + CurrencyFormatter.format(entry.getValue()));
                    } else {
                        lblTopCategoryName.setText("None yet");
                        lblTopCategoryAmount.setText("Record your first expense");
                    }

                    // Populate Recent Expenses list
                    populateRecentExpenses(recentExpenses);
                });

            } catch (DatabaseException e) {
                Platform.runLater(() -> {
                    lblTotalSpending.setText("₹ 0.00");
                    lblMonthSpending.setText("₹ 0.00");
                    lblTopCategoryName.setText("Database Offline");
                });
            }
        }).start();
    }

    /**
     * Dynamically renders the 5 most recent expenses as modern styled rows.
     */
    private void populateRecentExpenses(List<Expense> recentExpenses) {
        boxRecentExpenses.getChildren().clear();

        if (recentExpenses.isEmpty()) {
            lblRecentEmpty.setVisible(true);
            lblRecentEmpty.setManaged(true);
            boxRecentExpenses.getChildren().add(lblRecentEmpty);
            return;
        }

        lblRecentEmpty.setVisible(false);
        lblRecentEmpty.setManaged(false);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");

        for (Expense exp : recentExpenses) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #16191f; -fx-padding: 12 16 12 16; " +
                    "-fx-background-radius: 8px; -fx-border-color: #272c35; -fx-border-radius: 8px;");

            // Payment Icon Pill
            Label lblIcon = new Label(exp.getPaymentMethod().getIcon());
            lblIcon.setStyle("-fx-font-size: 16px; -fx-padding: 4 8 4 8; -fx-background-color: #1e2229; " +
                    "-fx-background-radius: 6px;");

            // Category & Note
            VBox detailsBox = new VBox(2);
            Label lblCategory = new Label(exp.getCategoryName());
            lblCategory.setStyle("-fx-text-fill: #f3f4f6; -fx-font-size: 13px; -fx-font-weight: bold;");

            String noteText = exp.getNote().isBlank() ? exp.getPaymentMethodName() : exp.getNote();
            Label lblNote = new Label(noteText);
            lblNote.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
            detailsBox.getChildren().addAll(lblCategory, lblNote);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Amount & Date
            VBox amountBox = new VBox(2);
            amountBox.setAlignment(Pos.CENTER_RIGHT);

            Label lblAmount = new Label(CurrencyFormatter.format(exp.getAmount()));
            lblAmount.setStyle("-fx-text-fill: #10b981; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label lblDate = new Label(exp.getDate().format(dtf));
            lblDate.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
            amountBox.getChildren().addAll(lblAmount, lblDate);

            row.getChildren().addAll(lblIcon, detailsBox, spacer, amountBox);
            boxRecentExpenses.getChildren().add(row);
        }
    }

    /**
     * Shortcut button handler navigating to Add Expense view.
     */
    @FXML
    private void handleQuickAdd(ActionEvent event) {
        if (MainController.getInstance() != null) {
            MainController.getInstance().showAddExpense();
        }
    }
}
