package org.example;

import jaxb.org.example.models.customers.Customers;
import jaxb.org.example.models.products.v1.ProductCatalogType;
import org.example.config.DBConnection;
import org.example.repository.CustomerRepository;
import org.example.repository.ProductRepository;
import org.example.xml.XmlDetector;
import org.example.xml.XmlParser;
import org.example.xml.XmlValidator;
import org.example.xml.XmlWriter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
public class Main {

    private static final String XSD_PATH = "src/main/resources/xsd";
    private static final String XML_PATH = "src/main/resources/xml";


    public static void main(String[] args) {

        if (!testDatabaseConnection()) return;

        //System.out.println(XmlDetector.detectVersion(XML_PATH+"/.xml"));

        xmlToDatabase(
                XML_PATH+"/product-v1.xml",
                XSD_PATH+"/product-v1.xsd",
                ProductCatalogType.class,
                ProductRepository::insert
        );

        databaseToXml(
                CustomerRepository::getAll,
                Customers.class,
                XML_PATH+ "/xml/customers.xml",
                XSD_PATH+ "/xsd/customers.xsd"
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