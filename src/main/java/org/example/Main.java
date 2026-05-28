package org.example;

import org.example.config.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        System.out.println("Connecting to database...");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Successfully connected to database.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to database.");
            System.err.println("Reason: " + e.getMessage());
        }
    }
}