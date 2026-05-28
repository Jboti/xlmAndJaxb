package org.example.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.example.customers.model.Customers;

import javax.xml.XMLConstants;
import java.io.File;

public class XmlWriter {

    public static void writeCustomers(Customers customers, String outputPath) {
        try {
            JAXBContext context = JAXBContext.newInstance(Customers.class);
            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(customers, new File(outputPath));

            System.out.println("Customers XML written to: " + outputPath);
        } catch (JAXBException e) {
            System.err.println("Failed Writing customers data into XML.");
            System.err.println(e.getMessage());
        }

    }

}
