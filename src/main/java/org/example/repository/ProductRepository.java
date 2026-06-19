package org.example.repository;

import org.example.config.DBConnection;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.sql.*;
import java.util.Arrays;
import java.util.GregorianCalendar;

public class ProductRepository {

    private static XMLGregorianCalendar toXmlGregorianCalendar(Date sqlDate) {
        try {
            return DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(sqlDate.toLocalDate().toString());
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static XMLGregorianCalendar toXmlGregorianCalendar(Timestamp ts) {
        if (ts == null) return null;
        try {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(ts);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static jaxb.org.example.models.products.v1.ProductCatalogType asV1(Object data) {
        return (jaxb.org.example.models.products.v1.ProductCatalogType) data;
    }

    private static jaxb.org.example.models.products.v2.ProductCatalogType asV2(Object data) {
        return (jaxb.org.example.models.products.v2.ProductCatalogType) data;
    }

    private static String getVersion(String version) {
        try {
            return switch (version) {
                case "v1" -> "v1";
                case "v2", "latest" -> "v2";
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
            case "v2" -> insertV2(asV2(data));
        }
    }

    public static <T> T read(String version) {
        final String validVersion = getVersion(version);
        if(null == validVersion) return null;

        return switch (validVersion) {
            case "v1" -> (T) readV1();
            case "v2" -> (T) readV2();
            default -> null;
        };
    }

    private static void insertV1(jaxb.org.example.models.products.v1.ProductCatalogType catalog){
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
                    stmt.setString(4,product.getCategory().value());
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

    private static jaxb.org.example.models.products.v1.ProductCatalogType readV1() {
        final String sql =
            """
                SELECT *
                FROM products;
            """;

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            jaxb.org.example.models.products.v1.ProductCatalogType catalog = new jaxb.org.example.models.products.v1.ProductCatalogType();

            while(rs.next()) {
                jaxb.org.example.models.products.v1.ProductType product =
                        new jaxb.org.example.models.products.v1.ProductType();

                product.setSku(rs.getString("sku"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setCategory(
                        jaxb.org.example.models.products.v1.CategoryType.fromValue(
                        rs.getString("category"))
                );
                product.setPrice(rs.getBigDecimal("price").toBigInteger());
                product.setStockQuantity(BigInteger.valueOf(rs.getLong("stock_quantity")));
                product.setWeightKg(rs.getBigDecimal("weight_kg"));
                product.setStatus(
                        jaxb.org.example.models.products.v1.StatusType.fromValue(
                        rs.getString("status"))
                );
                product.setInStock(rs.getBoolean("in_stock"));
                product.setReleaseDate(toXmlGregorianCalendar(rs.getDate("release_date")));
                product.setLastUpdated(toXmlGregorianCalendar(rs.getTimestamp("last_updated")));
                product.setRating(rs.getBigDecimal("rating"));
                product.setManufacturer(rs.getString("manufacturer"));
                product.setWarrantyYears(rs.getInt("warranty_years"));

                String[] tagArray = (String[]) rs.getArray("tags").getArray();

                jaxb.org.example.models.products.v1.TagsType tags =
                        new jaxb.org.example.models.products.v1.TagsType();

                tags.getTag().addAll(Arrays.asList(tagArray));
                product.setTags(tags);


                catalog.getProduct().add(product);
            }
            return catalog;
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
                    stmt.setString(4,product.getCategory().value());
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

    private static jaxb.org.example.models.products.v2.ProductCatalogType readV2() {
        final String sql =
                """
                    SELECT *
                    FROM products;
                """;
        final String reviewsSql =
                """
                    SELECT *
                    FROM product_reviews
                    WHERE product_sku = ?;
                """;

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            PreparedStatement reviewsStmt = conn.prepareStatement(reviewsSql);
            ResultSet rs = stmt.executeQuery()) {

            jaxb.org.example.models.products.v2.ProductCatalogType catalog = new jaxb.org.example.models.products.v2.ProductCatalogType();

            while(rs.next()) {
                jaxb.org.example.models.products.v2.ProductType product =
                        new jaxb.org.example.models.products.v2.ProductType();

                product.setSku(rs.getString("sku"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setCategory(
                        jaxb.org.example.models.products.v2.CategoryType.fromValue(
                                rs.getString("category"))
                );
                product.setPrice(rs.getBigDecimal("price").toBigInteger());
                product.setStockQuantity(BigInteger.valueOf(rs.getLong("stock_quantity")));
                product.setWeightKg(rs.getBigDecimal("weight_kg"));
                product.setStatus(
                        jaxb.org.example.models.products.v2.StatusType.fromValue(
                                rs.getString("status"))
                );
                product.setInStock(rs.getBoolean("in_stock"));
                product.setReleaseDate(toXmlGregorianCalendar(rs.getDate("release_date")));
                product.setLastUpdated(toXmlGregorianCalendar(rs.getTimestamp("last_updated")));
                product.setRating(rs.getBigDecimal("rating"));
                product.setManufacturer(rs.getString("manufacturer"));
                product.setWarrantyYears(rs.getInt("warranty_years"));

                String[] tagArray = (String[]) rs.getArray("tags").getArray();

                jaxb.org.example.models.products.v2.TagsType tags =
                        new jaxb.org.example.models.products.v2.TagsType();

                tags.getTag().addAll(Arrays.asList(tagArray));
                product.setTags(tags);

                jaxb.org.example.models.products.v2.ReviewsType reviews =
                        new jaxb.org.example.models.products.v2.ReviewsType();
                reviewsStmt.setString(1,product.getSku());
                ResultSet reviewsRs = reviewsStmt.executeQuery();
                while(reviewsRs.next()) {
                    jaxb.org.example.models.products.v2.ReviewsType.Review review =
                            new jaxb.org.example.models.products.v2.ReviewsType.Review();

                    review.setUsername(reviewsRs.getString("username"));
                    review.setComment(reviewsRs.getString("comment"));
                    review.setRating(reviewsRs.getInt("rating"));

                    reviews.getReview().add(review);
                }
                product.setReviews(reviews);

                catalog.getProduct().add(product);
            }
            return catalog;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
