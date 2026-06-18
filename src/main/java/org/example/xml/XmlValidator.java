package org.example.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

public class XmlValidator {

    public static boolean validate(String xmlPath, String xsdPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();

            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    System.err.println(XmlErrorFormatter.format(exception));
                    throw new SAXException();
                }

                @Override
                public void error(SAXParseException exception) throws SAXException{
                    System.err.println(XmlErrorFormatter.format(exception));
                    throw new SAXException();
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException{
                    System.err.println(XmlErrorFormatter.format(exception));
                    throw new SAXException();
                }
            });

            validator.validate(new StreamSource(new File(xmlPath)));

            System.out.println("XML is valid for schema.");
            return true;
        } catch (SAXException e) {
            return false;
        } catch (IOException e) {
            System.err.println("Could not read XML or XSD file:");
            System.err.println(e.getMessage());
            return false;
        }
    }
}