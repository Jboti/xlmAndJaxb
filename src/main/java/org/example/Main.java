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
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Main {

    private static final String RESOURCE_PATH = "src/main/resources";


    public static void main(String[] args) {

        if (!testDatabaseConnection()) return;

        xmlToDatabase(
                RESOURCE_PATH+"/product.xml",
                RESOURCE_PATH+"/product.xsd",
                ProductCatalog.class,
                ProductRepository::insert
        );

        databaseToXml(
                CustomerRepository::getAll,
                Customers.class,
                RESOURCE_PATH+"/customers.xml",
                RESOURCE_PATH+"/customers.xsd"
        );

    }

    private static <T> void xmlToDatabase(
            String xml,
            String xsd,
            Class<T> cls,
            Consumer<T> consumer
    ) {

        if(!XmlValidator.validate(xml,xsd)) return;

        T data = XmlParser.parse(cls, xml);

        if(data == null){
            System.err.println("Could not parse XML into Java Objects.");
            return;
        }
        System.out.println("Successfully parsed XML into Java Objects.");

        consumer.accept(data);

        System.out.println("Insertion finished.");
    }

    private static <T> void databaseToXml(
            Supplier<T> supplier,
            Class<T> cls,
            String outputPath,
            String xsd
    ) {

        T data = supplier.get();

        if(data == null) {
            System.err.println("Failed to fetch data from database.");
            return;
        }
        System.out.println("Successfully fetched data.");

        XmlWriter.write(cls, data, outputPath);

        if(!XmlValidator.validate(outputPath,xsd)) {
            System.err.println("Written XML is invalid for schema.");
            return;
        }

        System.out.println("Written XML is valid for schema.");
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