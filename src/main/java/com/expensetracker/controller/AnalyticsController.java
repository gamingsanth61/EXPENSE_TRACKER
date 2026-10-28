package com.expensetracker.controller;

import com.expensetracker.exception.DatabaseException;
import com.expensetracker.service.AnalyticsService;
import com.expensetracker.util.CurrencyFormatter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for Spending Analytics and Visualizations.
 * 
 * OOP Concepts Demonstrated:
 * 1. POLYMORPHISM & JAVAFX CHARTS HIERARCHY:
 *    Manipulates specialized chart types (PieChart, BarChart, LineChart) derived
 *    from javafx.scene.chart.Chart and XYChart.
 * 2. COLLECTIONS & GENERICS:
 *    Uses Map<String, Double>, ObservableList<PieChart.Data>, and XYChart.Series<String, Number>.
 * 3. EVENT-DRIVEN REACTIVITY:
 *    Reacts to Year ComboBox changes and installs dynamic interactive Tooltips on chart nodes.
 */
public class AnalyticsController {

    @FXML
    private ComboBox<Integer> comboYear;

    @FXML
    private Label lblAnnualTotal;

    @FXML
    private PieChart pieChartCategories;

    @FXML
    private BarChart<String, Number> barChartMonthly;

    @FXML
    private LineChart<String, Number> lineChartTrend;

    @FXML
    private Label lblNoData;

    private final AnalyticsService analyticsService = new AnalyticsService();

    @FXML
    public void initialize() {
        loadAvailableYears();
    }

    /**
     * Populates the year dropdown with distinct years from recorded transactions.
     */
    private void loadAvailableYears() {
        new Thread(() -> {
            try {
                List<Integer> years = analyticsService.getAvailableYears();
                Platform.runLater(() -> {
                    comboYear.setItems(FXCollections.observableArrayList(years));
                    if (!years.isEmpty()) {
                        comboYear.getSelectionModel().selectFirst();
                        loadChartsForYear(comboYear.getValue());
                    }
                    comboYear.valueProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            loadChartsForYear(newVal);
                        }
                    });
                });
            } catch (DatabaseException e) {
                Platform.runLater(() -> lblAnnualTotal.setText("Database Offline"));
            }
        }).start();
    }

    /**
     * Loads and updates all three visual charts for the specified calendar year.
     */
    private void loadChartsForYear(int year) {
        new Thread(() -> {
            try {
                Map<String, Double> categorySpending = analyticsService.getCategoryWiseSpending(year);
                Map<String, Double> monthlySpending = analyticsService.getMonthlyBreakdown(year);
                Map<String, Double> trendSpending = analyticsService.getCumulativeSpendingTrend(year);

                double totalAnnual = categorySpending.values().stream().mapToDouble(Double::doubleValue).sum();

                Platform.runLater(() -> {
                    lblAnnualTotal.setText("Total Spending in " + year + ": " + CurrencyFormatter.format(totalAnnual));

                    boolean hasData = totalAnnual > 0;
                    lblNoData.setVisible(!hasData);
                    lblNoData.setManaged(!hasData);

                    updatePieChart(categorySpending, totalAnnual);
                    updateBarChart(monthlySpending);
                    updateLineChart(trendSpending);
                });

            } catch (DatabaseException e) {
                Platform.runLater(() -> lblAnnualTotal.setText("Error loading analytics: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Builds the Category Distribution PieChart with percentage tooltips.
     */
    private void updatePieChart(Map<String, Double> categoryMap, double totalAnnual) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            if (entry.getValue() > 0) {
                double pct = (totalAnnual > 0) ? (entry.getValue() / totalAnnual) * 100.0 : 0.0;
                String label = String.format("%s (%.1f%%)", entry.getKey(), pct);
                PieChart.Data slice = new PieChart.Data(label, entry.getValue());
                pieData.add(slice);
            }
        }

        pieChartCategories.setData(pieData);

        // Attach interactive tooltips to each slice
        for (PieChart.Data slice : pieData) {
            String tooltipText = String.format("%s: %s", slice.getName(), CurrencyFormatter.format(slice.getPieValue()));
            Tooltip.install(slice.getNode(), new Tooltip(tooltipText));
        }
    }

    /**
     * Builds the Monthly Volume BarChart.
     */
    private void updateBarChart(Map<String, Double> monthlyMap) {
        barChartMonthly.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Spending");

        for (Map.Entry<String, Double> entry : monthlyMap.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);
        }

        barChartMonthly.getData().add(series);

        // Attach tooltips to each bar node once rendered
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                String tip = String.format("%s: %s", data.getXValue(), CurrencyFormatter.format(data.getYValue().doubleValue()));
                Tooltip.install(data.getNode(), new Tooltip(tip));
            }
        }
    }

    /**
     * Builds the Cumulative Spending Trend LineChart.
     */
    private void updateLineChart(Map<String, Double> trendMap) {
        lineChartTrend.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cumulative Trend");

        for (Map.Entry<String, Double> entry : trendMap.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);
        }

        lineChartTrend.getData().add(series);

        // Attach tooltips to each data point on the line
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                String tip = String.format("Through %s: %s", data.getXValue(), CurrencyFormatter.format(data.getYValue().doubleValue()));
                Tooltip.install(data.getNode(), new Tooltip(tip));
            }
        }
    }
}
