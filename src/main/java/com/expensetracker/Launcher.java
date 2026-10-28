package com.expensetracker;

/**
 * Standard JVM entry point for packaging and non-modular execution.
 * 
 * Why this class exists:
 * In modern Java (Java 11+), launching an application whose main class directly
 * extends javafx.application.Application without the JavaFX runtime on the boot-layer
 * can trigger a runtime launcher error ("JavaFX runtime components are missing").
 * By having this separate Launcher class without extending Application,
 * the JVM starts normally and allows JavaFX to initialize cleanly.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
