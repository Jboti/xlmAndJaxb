package org.example.xml;

import org.xml.sax.SAXParseException;

public class XmlErrorFormatter {

    public static String format(SAXParseException e) {
        if (null == e) {
            return "Ismeretlen XML átalakítási hiba";
        }

        final int line = e.getLineNumber();
        final int column = e.getColumnNumber();

        String message = e.getMessage();
        if (null == message || message.isBlank()) {
            return "XML validációs hiba.";
        }
        message = simplifyMessage(message.trim());

        if (line > 0 && column > 0) {
            return "[" + line + ".Sor:" + column + "] " + message;
        }

        return message;
    }


    private static String simplifyMessage(String message) {

        if (message.contains("cvc-elt.1") || message.contains("cvc-elt.1.a")) {
            String element = extractBetween(message, "element '", "'");

            if (element != null) {
                return "Nem található deklaráció a következő elemhez: '" + element + "'.";
            }

            return "Nem található deklaráció az elemhez";
        }

        if(message.contains("cvc-pattern-valid")) {
            String value = extractBetween(message, "Value '","'");
            String pattern = extractBetween(message, "pattern '","'");

            if(value != null && pattern != null) {
                return "A megadott érték: '" + value + "', nem felel meg a mező által kért mintának: '" + pattern + "'.";
            }
        }

        if(message.contains("cvc-minLength-valid")) {
            String value = extractBetween(message,"Value '","'");
            String min = extractBetween(message, "minLength '","'");

            if(value != null && min != null) {
                return "A megadott érték: '" + value + "' rövidebb, mint a megengedett minimum: " + min + ".";
            }

        }

        if(message.contains("cvc-maxLength-valid")) {
            String value = extractBetween(message,"Value '","'");
            String max = extractBetween(message, "maxLength '","'");

            if(value != null && max != null) {
                return "A megadott érték: '" + value + "' hosszabb, mint a megengedett maximum: " + max + ".";
            }

        }

        if (message.contains("cvc-minInclusive-valid")) {
            String value = extractBetween(message, "Value '", "'");
            String min = extractBetween(message, "minInclusive '", "'");

            if (value != null && min != null) {
                return "A megadott érték hibás! A minimális érték: " + min + ", megadva: "  + value;
            }
        }

        if(message.contains("cvc-minExclusive-valid")) {
            String value = extractBetween(message, "Value '", "'");
            String min = extractBetween(message, "minExclusive '", "'");

            if (value != null && min != null) {
                return "A megadott érték hibás! A minimális érték nem lehet: " + min + ", vagy kisebb. Megadva: "  + value;
            }
        }

        if (message.contains("cvc-maxInclusive-valid")) {
            String value = extractBetween(message, "Value '", "'");
            String max = extractBetween(message, "maxInclusive '", "'");

            if (value != null && max != null) {
                return "A megadott érték hibás! A maximális érték: " + max + ", megadva: "  + value;
            }
        }

        if(message.contains("cvc-maxExclusive-valid")) {
            String value = extractBetween(message, "Value '", "'");
            String max = extractBetween(message, "maxExclusive '", "'");

            if (value != null && max != null) {
                return "A megadott érték hibás! A maximális érték nem lehet: " + max + ", vagy nagyobb. Megadva: "  + value;
            }
        }

        if (message.contains("cvc-enumeration-valid")) {
            String value = extractBetween(message, "Value '", "'");
            String enums = extractBetween(message, "enumeration '","'");

            if (value != null) {
                return "A megadott érték: '"+value+"', nem szerepel a lehetséges értékek között: " + enums;
            }
        }

        if (message.contains("cvc-type.3.1.3")) {
            String value = extractBetween(message, "The value '", "'");
            if (value != null) {
                return "A megadott érték: '" + value + "', nem megfelelő típusú ennek a mezőnek";
            }
        }

        if (message.contains("cvc-complex-type.2.4.a")) {
            String actual = extractBetween(message, "starting with element '", "'");
            String expected = extractBetween(message, "One of '{", "}' is expected");

            if (actual != null && expected != null) {
                return "Az adott helyen '" + actual + "' nem várt elem, a várt elem: '" + expected + "'";
            }

            if (actual != null) {
                return "Az adott helyen '" + actual + "' nem várt elem.";
            }
        }

        if (message.contains("cvc-complex-type.2.4.b")) {
            String actual = extractBetween(message, "element '", "'");
            String expected = extractBetween(message, "One of '{", "}' is expected");

            if (actual != null && expected != null) {
                return "'" + actual + "' elem hiányos, a hiányzó elem: '" + expected + "'";
            }

            if (actual != null) {
                return "'" + actual + "' elem hiányos";
            }

            return "Szükséges gyermek elem hiányzik";
        }

        if (message.contains("cvc-fractionDigits-valid")) {
            String value = extractBetween(message, "Value '","'");
            String maxFractionDigits = extractBetween(message, "to ",".");

            if (value != null && maxFractionDigits != null) {
                return "Nem engedélyezett pontosságú tört rész: '" + value + "', a megengedett maximum pontosság: " + maxFractionDigits;
            }
        }

        if(message.contains("cvc-datatype-valid.1.2.1")) {
            String value = extractBetween(message,"'","'");
            String type = extractBetween(message,"for '","'");

            if(value != null && type != null) {
                return "Helytelen típus! A várt típus '" + type + "', a kapott érték: '" + value + "'.";
            }
        }

        return message;
    }

    private static String extractBetween(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        if (startIndex < 0) {
            return null;
        }

        startIndex += start.length();
        int endIndex = text.indexOf(end, startIndex);
        if (endIndex < 0) {
            return null;
        }

        return text.substring(startIndex, endIndex);
    }

}