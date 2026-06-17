package org.example.repository;

import org.example.config.DBConnection;
import org.example.customers.model.Customer;
import org.example.customers.model.Customers;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerRepository {


    public static Customers getAll() {
        String sql = """
            SELECT *
            FROM customers c
            ORDER BY c.id
        """;

        Customers customers = new Customers();

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customer customer = new Customer();

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
