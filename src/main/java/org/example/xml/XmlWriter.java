package org.example.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.example.customers.model.Customers;

import java.io.File;

public class XmlWriter {

    public static <T> void write(Class<T> cls, T data, String outputPath) {
        try {
            JAXBContext context = JAXBContext.newInstance(cls);
            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(data, new File(outputPath));

            System.out.println("Successfully written data into XML: " + outputPath);
        } catch (JAXBException e) {
            System.err.println("Failed writing data into XML.");
            System.err.println(e.getMessage());
        }

    }

}
