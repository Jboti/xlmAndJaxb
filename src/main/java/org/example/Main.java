package org.example;

import org.example.config.DBConnection;
import org.example.customers.model.Customers;
import org.example.products.model.Product;
import org.example.products.model.ProductCatalog;
import org.example.repository.CustomerRepository;
import org.example.repository.ProductRepository;
import org.example.xml.XmlParser;
import org.example.xml.XmlValidator;
import org.example.xml.XmlWriter;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    private static final String XML_PATH = "src/main/resources/products.xml";
    private static final String CUSTOMERS_OUTPUT_PATH = "src/main/resources/customers.xml";

    public static void main(String[] args) {

        //xmlToDatabase();
        databaseToXml();

    }

    private static void xmlToDatabase() {
        // Database Connection
        if (!testDatabaseConnection()) return;

        // XML Validation
        final boolean validXml = XmlValidator.validate(XML_PATH);

        if(!validXml){
            System.err.println("XML file is invalid.");
            return;
        }
        System.out.println("Xml is valid.");

        // XML Parsing
        ProductCatalog catalog = XmlParser.parse(XML_PATH);

        if(catalog == null){
            System.err.println("Could not parse XML into Java Objects.");
            return;
        }

        System.out.println("Successfully parsed XML into Java Objects.");

        /*
        System.out.println("Product count: " + catalog.getProduct().size());
        for(Product product : catalog.getProduct()){
            System.out.println();
            System.out.println("SKU: " + product.getSku());
            System.out.println("Name: " + product.getName());
            System.out.println("Category: " + product.getCategory());
            System.out.println("Price: " + product.getPrice() + " Ft");
        }
        */

        // Insert into Database

        for(Product product : catalog.getProduct()){
            ProductRepository.insertProduct(product);
        }

        System.out.println("Insert finished.");
    }

    private static void databaseToXml() {
        // Database Connection
        if (!testDatabaseConnection()) return;

        // Fetch data
        Customers customers = CustomerRepository.getAllCustomers();

        if(customers == null) {
            System.err.println("Couldnt fetch customers data from database.");
            return;
        }
        System.out.println("Successfully fetched customer data.");

        // Write data to XML
        XmlWriter.writeCustomers(customers,CUSTOMERS_OUTPUT_PATH );

    }


    private static boolean testDatabaseConnection() {
        System.out.println("Connecting to database...");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Successfully connected to database.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to database.");
            System.err.println("Reason: " + e.getMessage());
            return false;
        }
        return true;
    }
}