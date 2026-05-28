package org.example.xml;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

public class XmlValidator {

    private static final String XSD_PATH = "src/main/resources/products.xsd";

    public static boolean validate(String xmlPath){
        try{
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(XSD_PATH));
            Validator validator =  schema.newValidator();
            validator.validate(new StreamSource(new File(xmlPath)));

            return true;
        }catch(SAXException e){
            System.err.println("XML validation failed: ");
            System.err.println(e.getMessage());
            return false;
        } catch(IOException e){
            System.err.println("Could not read XML or XSD file: ");
            System.err.println(e.getMessage());
            return false;
        }
    }
}
