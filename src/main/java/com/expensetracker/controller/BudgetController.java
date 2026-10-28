package com.expensetracker.controller;

import com.expensetracker.exception.DatabaseException;
import com.expensetracker.model.BudgetProgress;
import com.expensetracker.model.Category;
import com.expensetracker.service.BudgetService;
import com.expensetracker.util.CurrencyFormatter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Controller for Budget Planning, setting limits, and tracking progress.
 * 
 * OOP Concepts Demonstrated:
 * 1. ENCAPSULATION & DATA TRANSFER OBJECTS (DTO):
 *    Processes BudgetProgress objects that encapsulate limit, spent, remaining,
 *    and threshold evaluation.
 * 2. REACTIVE USER INTERFACE:
 *    Dynamically generates progress bars that react to threshold conditions
 *    (Green for on-track, Amber for warning, Red for exceeded).
 */
public class BudgetController {

    @FXML
    private ComboBox<Category> comboCategory;

    @FXML
    private TextField txtLimit;

    @FXML
    private ComboBox<String> comboFormMonth;

    @FXML
    private ComboBox<Integer> comboFormYear;

    @FXML
    private Label lblFeedback;

    @FXML
    private ComboBox<String> comboFilterMonth;

    @FXML
    private ComboBox<Integer> comboFilterYear;

    @FXML
    private VBox boxBudgetCards;

    @FXML
    private Label lblEmptyBudgets;

    private final BudgetService budgetService = new BudgetService();
    private final List<String> monthNames = new ArrayList<>();

    @FXML
    public void initialize() {
        setupMonthAndYearDropdowns();
        loadCategories();
        setupFilterListeners();
        loadBudgetsForSelectedFilter();
    }

    private void setupMonthAndYearDropdowns() {
        for (int m = 1; m <= 12; m++) {
            monthNames.add(Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }

        LocalDate now = LocalDate.now();
        String currentMonthName = Month.of(now.getMonthValue()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int currentYear = now.getYear();

        List<Integer> years = List.of(currentYear - 1, currentYear, currentYear + 1);

        // Form dropdowns
        comboFormMonth.setItems(FXCollections.observableArrayList(monthNames));
        comboFormMonth.setValue(currentMonthName);

        comboFormYear.setItems(FXCollections.observableArrayList(years));
        comboFormYear.setValue(currentYear);

        // Filter dropdowns
        comboFilterMonth.setItems(FXCollections.observableArrayList(monthNames));
        comboFilterMonth.setValue(currentMonthName);

        comboFilterYear.setItems(FXCollections.observableArrayList(years));
        comboFilterYear.setValue(currentYear);
    }

    private void setupFilterListeners() {
        comboFilterMonth.valueProperty().addListener((obs, oldV, newV) -> loadBudgetsForSelectedFilter());
        comboFilterYear.valueProperty().addListener((obs, oldV, newV) -> loadBudgetsForSelectedFilter());
    }

    private void loadCategories() {
        new Thread(() -> {
            try {
                List<Category> categories = budgetService.getAllCategories();
                Platform.runLater(() -> {
                    comboCategory.setItems(FXCollections.observableArrayList(categories));
                    if (!categories.isEmpty()) {
                        comboCategory.getSelectionModel().selectFirst();
                    }
                });
            } catch (DatabaseException e) {
                Platform.runLater(() -> showFeedback("Database error loading categories.", false));
            }
        }).start();
    }

    @FXML
    private void handleSaveBudget(ActionEvent event) {
        hideFeedback();

        Category category = comboCategory.getValue();
        String limitStr = txtLimit.getText();
        int month = comboFormMonth.getSelectionModel().getSelectedIndex() + 1;
        Integer year = comboFormYear.getValue();

        if (year == null) {
            showFeedback("Please select a valid year.", false);
            return;
        }

        try {
            budgetService.setBudget(category, limitStr, month, year);
            showFeedback("✓ Budget saved for " + category.getName() + " (" + CurrencyFormatter.format(Double.parseDouble(limitStr)) + ")", true);
            txtLimit.clear();

            // Synchronize filter if saved for current filter selection
            comboFilterMonth.setValue(comboFormMonth.getValue());
            comboFilterYear.setValue(year);
            loadBudgetsForSelectedFilter();

        } catch (IllegalArgumentException e) {
            showFeedback("✕ " + e.getMessage(), false);
        } catch (DatabaseException e) {
            showFeedback("✕ Database error: " + e.getMessage(), false);
        }
    }

    private void loadBudgetsForSelectedFilter() {
        int month = comboFilterMonth.getSelectionModel().getSelectedIndex() + 1;
        Integer year = comboFilterYear.getValue();
        if (month < 1 || year == null) return;

        new Thread(() -> {
            try {
                List<BudgetProgress> progressList = budgetService.getBudgetProgressList(month, year);
                Platform.runLater(() -> renderBudgetCards(progressList));
            } catch (DatabaseException e) {
                Platform.runLater(() -> boxBudgetCards.getChildren().clear());
            }
        }).start();
    }

    private void renderBudgetCards(List<BudgetProgress> list) {
        boxBudgetCards.getChildren().clear();

        if (list.isEmpty()) {
            lblEmptyBudgets.setVisible(true);
            lblEmptyBudgets.setManaged(true);
            boxBudgetCards.getChildren().add(lblEmptyBudgets);
            return;
        }

        lblEmptyBudgets.setVisible(false);
        lblEmptyBudgets.setManaged(false);

        for (BudgetProgress bp : list) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: #16191f; -fx-padding: 16; -fx-background-radius: 8px; " +
                    "-fx-border-color: #272c35; -fx-border-radius: 8px;");

            // Row 1: Category Name, Status Badge, Delete Button
            HBox topRow = new HBox(10);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label lblCategory = new Label(bp.getCategoryName());
            lblCategory.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");

            Label lblBadge = new Label();
            if (bp.isExceeded()) {
                lblBadge.setText("🚨 Exceeded by " + bp.getFormattedExceeded());
                lblBadge.getStyleClass().add("badge-exceeded");
            } else if (bp.isNearLimit()) {
                lblBadge.setText(String.format("⚠️ %.0f%% Used", bp.getPercentage()));
                lblBadge.getStyleClass().add("badge-near-limit");
            } else {
                lblBadge.setText(String.format("✓ %.0f%% (On Track)", bp.getPercentage()));
                lblBadge.getStyleClass().add("badge-on-track");
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnDelete = new Button("🗑️");
            btnDelete.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #ef4444; -fx-font-size: 14px;");
            btnDelete.setOnAction(e -> handleDeleteBudget(bp));

            topRow.getChildren().addAll(lblCategory, lblBadge, spacer, btnDelete);

            // Row 2: Progress Bar
            ProgressBar progressBar = new ProgressBar(bp.getProgressFraction());
            progressBar.setMaxWidth(Double.MAX_VALUE);
            if (bp.isExceeded()) {
                progressBar.getStyleClass().add("progress-exceeded");
            } else if (bp.isNearLimit()) {
                progressBar.getStyleClass().add("progress-warning");
            } else {
                progressBar.getStyleClass().add("progress-normal");
            }

            // Row 3: Spent / Limit and Remaining Amount
            HBox infoRow = new HBox(10);
            infoRow.setAlignment(Pos.CENTER_LEFT);

            Label lblSpent = new Label("Spent: " + bp.getFormattedSpent() + " / " + bp.getFormattedLimit());
            lblSpent.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");

            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);

            Label lblRemaining = new Label(bp.isExceeded() ?
                    "Budget Deficit: -" + bp.getFormattedExceeded() :
                    "Remaining: " + bp.getFormattedRemaining());
            lblRemaining.setStyle(bp.isExceeded() ?
                    "-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold;" :
                    "-fx-text-fill: #10b981; -fx-font-size: 12px; -fx-font-weight: bold;");

            infoRow.getChildren().addAll(lblSpent, spacer2, lblRemaining);

            card.getChildren().addAll(topRow, progressBar, infoRow);
            boxBudgetCards.getChildren().add(card);
        }
    }

    private void handleDeleteBudget(BudgetProgress bp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Budget");
        confirm.setHeaderText("Remove budget for " + bp.getCategoryName() + "?");
        confirm.setContentText("Are you sure you want to remove the monthly limit of " + bp.getFormattedLimit() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                budgetService.deleteBudget(bp.getBudget().getId());
                loadBudgetsForSelectedFilter();
            } catch (DatabaseException e) {
                showFeedback("Error deleting budget: " + e.getMessage(), false);
            }
        }
    }

    private void showFeedback(String message, boolean success) {
        lblFeedback.setText(message);
        lblFeedback.setVisible(true);
        lblFeedback.setManaged(true);
        lblFeedback.getStyleClass().removeAll("feedback-success", "feedback-error");
        lblFeedback.getStyleClass().add(success ? "feedback-success" : "feedback-error");
    }

    private void hideFeedback() {
        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);
    }
}
