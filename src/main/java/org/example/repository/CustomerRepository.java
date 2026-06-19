package org.example.repository;

import org.example.config.DBConnection;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.sql.*;

public class CustomerRepository {

    private static jaxb.org.example.models.customers.v1.Customers asV1(Object data) {
        return (jaxb.org.example.models.customers.v1.Customers) data;
    }

    private static String getVersion(String version) {
        try {
            return switch (version) {
                case "v1", "latest" -> "v1";
                default -> throw new IllegalArgumentException();
            };
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid version for product.");
            return null;
        }
    }

    public static <T> void insert(T data, String version) {
        final String validVersion = getVersion(version);
        if(null == validVersion) return;

        switch (validVersion) {
            case "v1" -> insertV1(asV1(data));
        }
    }

    public static <T> T read(String version) {
        final String validVersion = getVersion(version);
        if (null == validVersion) return null;

        return switch (validVersion) {
            case "v1" -> (T) readV1();
            default -> null;
        };
    }


    private static void insertV1(jaxb.org.example.models.customers.v1.Customers customers) {
        final String sql =
                """
                    INSERT INTO customers(
                        customer_code,full_name,email,city,age,active,signup_date
                    ) VALUES (?,?,?,?,?,?,?)
                """;
        final String truncateSql = "TRUNCATE TABLE customers;";

        try(Connection conn = DBConnection.getConnection()){

            conn.setAutoCommit(false);

            try(PreparedStatement stmt = conn.prepareStatement(sql);
                PreparedStatement truncateStmt = conn.prepareStatement(truncateSql)) {

                truncateStmt.executeUpdate();

                for(jaxb.org.example.models.customers.v1.Customer customer : customers.getCustomer()) {

                    stmt.setString(1,customer.getCustomerCode());
                    stmt.setString(2,customer.getFullName());
                    stmt.setString(3,customer.getEmail());
                    stmt.setString(4,customer.getCity());
                    BigInteger customerAge = customer.getAge();
                    if(null == customerAge) {
                        stmt.setNull(5, Types.NUMERIC);
                    } else {
                        stmt.setInt(5,customerAge.intValue());
                    }
                    stmt.setBoolean(6,customer.isActive());
                    stmt.setDate(7,
                            new Date(customer.getSignupDate().toGregorianCalendar().getTime().getTime())
                    );

                    stmt.addBatch();
                }
                stmt.executeBatch();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
                System.err.println("Failed statement, rolling back...");
                conn.rollback();
            }

            conn.commit();

        } catch (SQLException e) {
            System.err.println("Insert failed.");
        }
    }

    private static jaxb.org.example.models.customers.v1.Customers readV1() {
        String sql = """
            SELECT *
            FROM customers c;
            """;

        jaxb.org.example.models.customers.v1.Customers customers =
                new jaxb.org.example.models.customers.v1.Customers();

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                jaxb.org.example.models.customers.v1.Customer customer =
                        new jaxb.org.example.models.customers.v1.Customer();

                customer.setCustomerCode(rs.getString("customer_code"));
                customer.setFullName(rs.getString("full_name"));
                customer.setEmail(rs.getString("email"));
                customer.setCity(rs.getString("city"));

                final int age = rs.getInt("age");
                if(!rs.wasNull()) {
                    customer.setAge(BigInteger.valueOf(age));
                }

                customer.setActive(rs.getBoolean("active"));
                try{
                    customer.setSignupDate(
                            DatatypeFactory
                            .newInstance()
                            .newXMLGregorianCalendar(rs.getDate("signup_date").toString())
                    );
                } catch (DatatypeConfigurationException e) {
                    System.err.println(e.getMessage());
                    continue;
                }

                customers.getCustomer().add(customer);
            }
        } catch (SQLException e) {
          System.err.println("Failed to fetch customer data.");
          System.err.println(e.getMessage());
          return null;
        }
        return customers;
    }
}
