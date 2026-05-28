package org.example.repository;

import org.example.config.DBConnection;
import org.example.model.Product;

import java.math.BigDecimal;
import java.sql.*;

public class ProductRepository {

    public static void insertProduct(Product product){
        String sql =
        """
        INSERT INTO products(
            sku, name, description, category, price,
            stock_quantity, weight_kg, status,
            in_stock, release_date, last_updated, rating,
            tags, manufacturer, warranty_years
        ) VALUES (
            ?, ?, ?, ?, ?, ?, ?, ?::product_status, ?, ?, ?, ?, ?, ?, ?
        );
        """;

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1,product.getSku());
            stmt.setString(2,product.getName());
            stmt.setString(3,product.getDescription());
            stmt.setString(4,product.getCategory().toString());
            stmt.setBigDecimal(5,new java.math.BigDecimal(product.getPrice()));
            stmt.setInt(6, product.getStockQuantity().intValue());
            if(product.getWeightKg() != null) {
                stmt.setBigDecimal(7, product.getWeightKg());
            } else {
                stmt.setNull(7, Types.NUMERIC);
            }
            stmt.setString(8, product.getStatus());
            stmt.setBoolean(9, product.isInStock());
            stmt.setDate(10, Date.valueOf(product.getReleaseDate().toString()));
            stmt.setTimestamp(11,
                    Timestamp.valueOf(
                        product.getLastUpdated()
                        .toGregorianCalendar()
                        .toZonedDateTime()
                        .toLocalDateTime()
                    )
            );
            if(product.getRating() != null) {
                stmt.setBigDecimal(12, product.getRating());
            } else {
                stmt.setNull(12, Types.NUMERIC);
            }
            Array tagsArray = conn.createArrayOf(
            "text",
                    product.getTags().getTag().toArray(new String[0])
            );
            stmt.setArray(13, tagsArray);
            stmt.setString(14, product.getManufacturer());
            stmt.setInt(15, product.getWarrantyYears());

            stmt.executeUpdate();

        } catch(SQLException e) {
            System.err.println("Insert failed.");
            System.err.println(e.getMessage());
        }
    }
}
