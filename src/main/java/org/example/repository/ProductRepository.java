package org.example.repository;

import jaxb.org.example.models.products.v1.ProductCatalogType;
import jaxb.org.example.models.products.v1.ProductType;
import jaxb.org.example.models.products.v2.ReviewsType;
import org.example.config.DBConnection;

import java.math.BigDecimal;
import java.sql.*;

public class ProductRepository {

    private static jaxb.org.example.models.products.v1.ProductCatalogType asV1(Object data) {
        return (jaxb.org.example.models.products.v1.ProductCatalogType) data;
    }

    private static jaxb.org.example.models.products.v2.ProductCatalogType asV2(Object data) {
        return (jaxb.org.example.models.products.v2.ProductCatalogType) data;
    }

    public static <T> void insert(T data, String version) {
        try {
            switch (version) {
                case "v1" -> insertV1(asV1(data));
                case "v2" -> insertV2(asV2(data));
                default -> throw new IllegalArgumentException();
            }
        } catch(IllegalArgumentException e) {
            System.err.println("Invalid version for product.");
        }
    }

    public static void insertV1(jaxb.org.example.models.products.v1.ProductCatalogType catalog){
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

                for(jaxb.org.example.models.products.v1.ProductType product : catalog.getProduct()) {
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
                    stmt.setString(8, product.getStatus().value());
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
                System.err.println(e.getMessage());
                System.err.println("Failed statement, rolling back...");
                conn.rollback();
            }

            conn.commit();

        } catch (SQLException e) {
            System.err.println("Insert failed.");
        }
    }

    private static void insertV2(jaxb.org.example.models.products.v2.ProductCatalogType catalog) {
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

        final String truncateSql = "TRUNCATE TABLE products, product_reviews;";

        final String reviewSql =
                """
                INSERT INTO product_reviews(
                    product_sku, username, comment, rating
                ) VALUES (?,?,?,?);
                """;

        try(Connection conn = DBConnection.getConnection()){

            conn.setAutoCommit(false);
            try(PreparedStatement stmt = conn.prepareStatement(sql);
                PreparedStatement truncateStmt = conn.prepareStatement(truncateSql);
                PreparedStatement reviewStmt = conn.prepareStatement(reviewSql)) {

                truncateStmt.executeUpdate();

                for(jaxb.org.example.models.products.v2.ProductType product : catalog.getProduct()) {
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
                    stmt.setString(8, product.getStatus().value());
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

                    if(product.getReviews() != null) {
                        for(jaxb.org.example.models.products.v2.ReviewsType.Review review : product.getReviews().getReview()) {
                            reviewStmt.setString(1,product.getSku());
                            reviewStmt.setString(2,review.getUsername());
                            reviewStmt.setString(3,review.getComment());
                            reviewStmt.setInt(4, review.getRating());

                            reviewStmt.addBatch();
                        }
                    }
                }

                stmt.executeBatch();
                reviewStmt.executeBatch();
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
}
