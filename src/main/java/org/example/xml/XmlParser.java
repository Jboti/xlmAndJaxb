package org.example.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.example.model.ProductCatalog;

import java.io.File;

public class XmlParser {

    public static ProductCatalog parse(String xmlPath){
        try{
            JAXBContext context = JAXBContext.newInstance(ProductCatalog.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            return (ProductCatalog) unmarshaller.unmarshal(new File(xmlPath));
        }catch(JAXBException e){
            System.err.println("Failed to unmarshal XML: ");
            System.err.println(e.getMessage());
            return null;
        }
    }

}
