package org.example;

import jaxb.org.example.models.customers.Customers;
import jaxb.org.example.models.products.v2.ProductCatalogType;
import org.example.config.DBConnection;
import org.example.repository.CustomerRepository;
import org.example.repository.ProductRepository;
import org.example.xml.XmlDetector;
import org.example.xml.XmlParser;
import org.example.xml.XmlValidator;
import org.example.xml.XmlWriter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
public class Main {

    private static final String XSD_PATH = "src/main/resources/xsd";
    private static final String XML_PATH = "src/main/resources/xml";


    public static void main(String[] args) {

        if (!testDatabaseConnection()) return;

        xmlToDatabase(
                XML_PATH+"/productsV2.xml",
                XSD_PATH+"/products",
                "products",
                "ProductCatalogType",
                ProductRepository::insert
        );

        databaseToXml(
                CustomerRepository::getAll,
                Customers.class,
                XML_PATH+ "/customers.xml",
                XSD_PATH+ "/customers"
        );

    }

    private static <T> void xmlToDatabase(
            String xml,
            String xsd,
            String packageName,
            String className,
            BiConsumer<T,String> consumer
    ) {

        String version = XmlDetector.detectVersion(xml);

        String completeXsd = xsd+"-"+version+".xsd";
        if(!XmlValidator.validate(xml, completeXsd)) return;

        String fullClassName = "jaxb.org.example.models." + packageName + "." + version.toLowerCase() + "." + className;
        Class<T> cls = null;
        try {
            cls = (Class<T>) Class.forName(fullClassName);
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to get class for parsing.");
        }
        if(null == cls) return;

        T data = XmlParser.parse(cls, xml);

        if(data == null){
            System.err.println("Could not parse XML into Java Objects.");
            return;
        }
        System.out.println("Successfully parsed XML into Java Objects.");

        consumer.accept(data, version);

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

        if(!XmlValidator.validate(outputPath,xsd+".xsd")) {
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