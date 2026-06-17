package org.example.repository;

import jaxb.org.example.models.products.v1.ProductCatalogType;
import jaxb.org.example.models.products.v1.ProductType;
import org.example.config.DBConnection;

import java.sql.*;

public class ProductRepository {

    public static void insert(ProductCatalogType catalog){
        final String sql =
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

        final String truncateSql = "TRUNCATE TABLE products;";

        try(Connection conn = DBConnection.getConnection()){

            conn.setAutoCommit(false);
            try(PreparedStatement stmt = conn.prepareStatement(sql);
                PreparedStatement truncateStmt = conn.prepareStatement(truncateSql)) {

                truncateStmt.executeUpdate();

                for(ProductType product : catalog.getProduct()) {
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
                    stmt.setString(8, product.getStatus().name());
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

                    stmt.addBatch();
                }

                stmt.executeBatch();
            } catch (SQLException e) {
                System.err.println("Failed statement, rolling back...");
                conn.rollback();
            }

            conn.commit();

        } catch (SQLException e) {
            System.err.println("Insert failed.");
        }
    }
}
