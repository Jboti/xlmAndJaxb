package org.example.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

public class XmlParser {

    public static <T> T parse(Class<T> cls, String xmlPath){
        try{
            JAXBContext context = JAXBContext.newInstance(cls);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            return (T) unmarshaller.unmarshal(new File(xmlPath));
        }catch(JAXBException e){
            System.err.println("Failed to unmarshal XML: ");
            System.err.println(e.getMessage());
            return null;
        }
    }

}
