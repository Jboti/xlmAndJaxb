package org.example.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class XmlDetector {

    private XmlDetector() {}

    public static String detectVersion(String xmlPath) {

        try {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            XMLStreamReader reader = factory.createXMLStreamReader(new StreamSource(new File(xmlPath)));

            while(reader.hasNext()) {
                if(reader.next() == XMLStreamConstants.START_ELEMENT) {
                    String version = reader.getAttributeValue(null,"version");

                    if(null == version || version.isBlank()) {
                        System.err.println("Missing root version attribute");
                        return "";
                    }
                    return version;
                }
            }
            throw new IllegalArgumentException("No root element found.");
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to detect XML version",e);
        }

    }
}
