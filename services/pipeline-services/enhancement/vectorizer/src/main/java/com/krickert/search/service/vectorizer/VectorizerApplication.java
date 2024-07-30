package com.krickert.search.service.vectorizer;

import io.micronaut.runtime.Micronaut;

/**
 * The Application class is responsible for running the Micronaut application.
 * It contains the main method that initializes and starts the Micronaut framework.
 * <p>
 * Usage:
 * To run the application, simply execute the main method of this class. It will start the Micronaut framework and initialize the application.
 * <p>
 * Example:
 * Application.main(args);
 * <p>
 * Requirements:
 * - Micronaut framework must be installed in the project dependencies.
 */
public class VectorizerApplication {

    /**
     * The main method is responsible for running the Micronaut application.
     *
     * @param args The command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        Micronaut.run(VectorizerApplication.class, args);
    }
}