package database;

import entities.Manufacturer;
import entities.Product;
import entities.Review;
import enums.Country;
import generators.ManufacturerGenerator;
import generators.ProductGenerator;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String SSH_HOST = "helios.cs.ifmo.ru";
    private static final int SSH_PORT = 2222;
    private static final String SSH_USER = "s312486";
    private static final String SSH_PASSWORD = "SjFy}5114";

    private static final String DB_HOST = "pg";
    private static final int DB_PORT = 5432;
    private static final String DB_USER = "s312486";
    private static final String DB_PASSWORD = "YQb3LAuYg5o5jHOK";
    private static final String DB_NAME = "studs";

    private static final int LOCAL_PORT = 54321; // Локальный порт для туннеля

    private static Session session;

    public static void setupSshTunnel() throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
        session.setPassword(SSH_PASSWORD);

        // Настройка проверки хоста (отключение проверки)
        session.setConfig("StrictHostKeyChecking", "no");

        session.connect();

        // Проброс порта
        session.setPortForwardingL(LOCAL_PORT, DB_HOST, DB_PORT);
        System.out.println("SSH tunnel established.");
    }

    public static Connection connect() throws Exception {
        // Подключение к локальному проброшенному порту
        String url = "jdbc:postgresql://localhost:" + LOCAL_PORT + "/" + DB_NAME;
        return DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
    }

    public static void closeSshTunnel() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("SSH tunnel closed.");
        }
    }

    public static void dropTables(Connection connection) throws Exception {
        log("Dropping tables and index...");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS Review CASCADE;");
            statement.executeUpdate("DROP TABLE IF EXISTS Product CASCADE;");
            statement.executeUpdate("DROP TABLE IF EXISTS Manufacturer CASCADE;");
            statement.executeUpdate("DROP TABLE IF EXISTS Country;");
            log("Tables and index dropped.");
        }
    }


    public static void createTables(Connection connection) throws Exception {
        log("Creating tables and index...");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE Country (" +
                    "    id SERIAL PRIMARY KEY," +
                    "    name VARCHAR(50) NOT NULL UNIQUE" +
                    ");");

            statement.executeUpdate("CREATE TABLE Manufacturer (" +
                    "    id SERIAL PRIMARY KEY," +
                    "    name VARCHAR(255) NOT NULL," +
                    "    foundation_date DATE NOT NULL" +
                    ");");

            statement.executeUpdate("CREATE TABLE Product (" +
                    "    id SERIAL PRIMARY KEY," +
                    "    price NUMERIC(10, 2) NOT NULL," +
                    "    awg_rating NUMERIC(10, 3) DEFAULT 0," +
                    "    review_count INT DEFAULT 0," +
                    "    name VARCHAR(255) NOT NULL," +
                    "    release_date DATE NOT NULL," +
                    "    country_id INT NOT NULL REFERENCES Country(id)," +
                    "    manufacturer_id INT NOT NULL REFERENCES Manufacturer(id)" +
                    ");");

            statement.executeUpdate("CREATE TABLE Review (" +
                    "    id SERIAL PRIMARY KEY," +
                    "    date_time TIMESTAMP WITH TIME ZONE NOT NULL," +
                    "    user_name VARCHAR(255) NOT NULL," +
                    "    rating INT CHECK (rating >= 1 AND rating <= 5)," +
                    "    product_id INT NOT NULL REFERENCES Product(id)" +
                    ");");

            statement.executeUpdate("CREATE INDEX idx_product_reviews ON Review (product_id);");
            log("Tables and index created.");
        }
    }


    public static void fillCountryTable(Connection connection) throws Exception {
        log("Filling Country table...");
        try (PreparedStatement countryStmt = connection.prepareStatement(
                "INSERT INTO Country (name) VALUES (?)")) {
            for (Country country : Country.values()) {
                countryStmt.setString(1, country.name());
                countryStmt.addBatch();
            }
            countryStmt.executeBatch();
        }
        log("Country table filled.");
    }

    private static void updateProductRating(Connection connection, int productId, double rating) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE Product SET awg_rating = ? WHERE id = ?")) {
            stmt.setDouble(1, rating);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }



    public static void fillTables(Connection connection, int manufacturerCount, int productsCount, int reviewsPerProduct) throws Exception {
        log("Filling tables with generated data...");

        fillCountryTable(connection);

        log("Generating " + manufacturerCount + " rows for Manufacturer table...");
        ManufacturerGenerator manufacturerGenerator = new ManufacturerGenerator();
        List<Manufacturer> manufacturers = manufacturerGenerator.generateList(manufacturerCount);

        log("Filling in the Manufacturer table...");
        try (PreparedStatement manufacturerStmt = connection.prepareStatement(
                "INSERT INTO Manufacturer (name, foundation_date) VALUES (?, ?) RETURNING id")) {
            for (Manufacturer manufacturer : manufacturers) {
                manufacturerStmt.setString(1, manufacturer.name());
                manufacturerStmt.setDate(2, java.sql.Date.valueOf(manufacturer.foundationDate()));
                manufacturerStmt.execute();
            }
        }
        log("Manufacturer table filled.");

        log("Generating " + productsCount + " rows for Product table...");
        ProductGenerator productGenerator = new ProductGenerator(manufacturers, reviewsPerProduct);
        List<Product> products = productGenerator.generateList(productsCount);

        log("Filling in the Product table...");
        try (PreparedStatement productStmt = connection.prepareStatement(
                "INSERT INTO Product (price, review_count, name, release_date, country_id, manufacturer_id) VALUES (?, ?, ?, ?, ?, ?) RETURNING id")) {
            for (Product product : products) {
                productStmt.setBigDecimal(1, BigDecimal.valueOf(product.getPrice()));
                productStmt.setInt(2, reviewsPerProduct);
                productStmt.setString(3, product.getName());
                productStmt.setDate(4, java.sql.Date.valueOf(product.getReleaseDate()));
                productStmt.setInt(5, getCountryId(connection, product.getCountry())); // Get country_id from Country table
                productStmt.setInt(6, product.getManufacturer().id());
                productStmt.execute();
            }
        }
        log("Product table filled.");

        log("Filling in the Review table...");
        try (PreparedStatement reviewStmt = connection.prepareStatement(
                "INSERT INTO Review (date_time, user_name, rating, product_id) VALUES (?, ?, ?, ?)")) {
            for (Product product : products) {
                double ratingSum = 0;
                for (Review review : product.getReviews()) {
                    reviewStmt.setTimestamp(1, java.sql.Timestamp.from(review.getDateTime().toInstant()));
                    reviewStmt.setString(2, review.getUser());
                    reviewStmt.setInt(3, review.getRating());
                    reviewStmt.setInt(4, product.getId());
                    reviewStmt.addBatch();
                    ratingSum += review.getRating();
                }
                reviewStmt.executeBatch();
                double averageRating = (reviewsPerProduct > 0) ? ratingSum / reviewsPerProduct : 0;
                updateProductRating(connection, product.getId(), averageRating);
            }
        }
        log("Review table filled.");

        log("All tables filled with data.");
    }



    public static void log(String message) {
        System.out.println("[LOG] " + message);
    }

    private static int getCountryId(Connection connection, Country country) throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id FROM Country WHERE name = ?")) {
            stmt.setString(1, country.name());
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                } else {
                    throw new IllegalStateException("Country not found: " + country);
                }
            }
        }
    }

}

