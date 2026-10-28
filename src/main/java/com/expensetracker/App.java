package com.expensetracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application Class.
 * 
 * OOP Concept: INHERITANCE (Application lifecycle)
 * Why this class exists:
 * Serves as the entry point for the JavaFX runtime lifecycle.
 * The JavaFX platform invokes init(), start(Stage primaryStage), and stop().
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/expensetracker/view/MainView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1080, 720);

            primaryStage.setTitle("Expense Tracker with Analytics (₹)");
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(600);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
