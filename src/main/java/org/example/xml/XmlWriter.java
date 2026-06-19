package org.example.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.xml.namespace.QName;
import java.io.File;

public class XmlWriter {

    public static void write(Object data, String outputPath) {
        if (data == null) {
            System.err.println("Cannot write null data.");
            return;
        }

        try {
            Class<?> cls = data.getClass();

            JAXBContext context = JAXBContext.newInstance(cls);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            if (cls.isAnnotationPresent(XmlRootElement.class)) {
                marshaller.marshal(data, new File(outputPath));
            } else {
                String rootName = decapitalize(cls.getSimpleName());
                QName qName = new QName("", rootName);

                JAXBElement<?> root = new JAXBElement<>(qName, (Class) cls, data);
                marshaller.marshal(root, new File(outputPath));
            }

            System.out.println("Successfully written data into XML: " + outputPath);
        } catch (JAXBException e) {
            System.err.println("Failed writing data into XML.");
            System.err.println(e.getMessage());
        }
    }

    private static String decapitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}