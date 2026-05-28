package org.example;

import org.example.config.DBConnection;
import org.example.xml.XmlValidator;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    private static final String XML_PATH = "src/main/resources/products.xml";

    public static void main(String[] args) {

        // Database Connection
        System.out.println("Connecting to database...");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Successfully connected to database.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to database.");
            System.err.println("Reason: " + e.getMessage());
            return;
        }

        // XML Validation
        final boolean validXml = XmlValidator.validate(XML_PATH);

        if(!validXml){
            System.err.println("XML file is invalid.");
            return;
        }
        System.out.println("Xml is valid.");


    }
}