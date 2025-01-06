package database;

import entities.Manufacturer;
import entities.Product;
import entities.Review;
import generators.ManufacturerGenerator;
import generators.ProductGenerator;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
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
            log("Tables and index dropped.");
        }
    }

    public static void createTables(Connection connection) throws Exception {
        log("Creating tables and index...");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE Manufacturer (\n" +
                    "    id SERIAL PRIMARY KEY,\n" +
                    "    name VARCHAR(255) NOT NULL,\n" +
                    "    foundation_date DATE NOT NULL\n" +
                    ");");

            statement.executeUpdate("CREATE TABLE Product (\n" +
                    "    id SERIAL PRIMARY KEY,\n" +
                    "    price NUMERIC(10, 2) NOT NULL,\n" +
                    "    name VARCHAR(255) NOT NULL,\n" +
                    "    release_date DATE NOT NULL,\n" +
                    "    country VARCHAR(255) NOT NULL,\n" +
                    "    manufacturer_id INT NOT NULL REFERENCES Manufacturer(id)\n" +
                    ");");

            statement.executeUpdate("CREATE TABLE Review (\n" +
                    "    id SERIAL PRIMARY KEY,\n" +
                    "    date_time TIMESTAMP WITH TIME ZONE NOT NULL,\n" +
                    "    user_name VARCHAR(255) NOT NULL,\n" +
                    "    rating INT CHECK (rating >= 1 AND rating <= 5),\n" +
                    "    product_id INT NOT NULL REFERENCES Product(id)\n" +
                    ");");

            statement.executeUpdate("CREATE INDEX idx_product_reviews ON Review (product_id);");
            log("Tables and index created.");
        }
    }

    public static void fillTables(Connection connection, int manufacturerCount, int productsCount, int reviewsPerProduct) throws Exception {
        log("Filling tables with generated data...");

        log("Generating " + manufacturerCount + " rows for Manufacturer table...");
        ManufacturerGenerator manufacturerGenerator = new ManufacturerGenerator();
        List<Manufacturer> manufacturers = manufacturerGenerator.generateList(manufacturerCount);

        log("Filling in the table of Manufactures started...");
        try (PreparedStatement manufacturerStmt = connection.prepareStatement(
                "INSERT INTO Manufacturer (name, foundation_date) VALUES (?, ?) RETURNING id")) {
            for (Manufacturer manufacturer : manufacturers) {
                manufacturerStmt.setString(1, manufacturer.name());
                manufacturerStmt.setDate(2, java.sql.Date.valueOf(manufacturer.foundationDate()));
                manufacturerStmt.execute();
            }
        }
        log("The table of Manufactures is filled in.");

        log("Generating " + productsCount + " rows for Product table...");
        ProductGenerator productGenerator = new ProductGenerator(manufacturers, reviewsPerProduct);
        List<Product> products = productGenerator.generateList(productsCount);

        log("Filling in the table of Product started...");
        try (PreparedStatement productStmt = connection.prepareStatement(
                "INSERT INTO Product (price, name, release_date, country, manufacturer_id) VALUES (?, ?, ?, ?, ?) RETURNING id")) {
            for (Product product : products) {
                productStmt.setBigDecimal(1, BigDecimal.valueOf(product.getPrice()));
                productStmt.setString(2, product.getName());
                productStmt.setDate(3, java.sql.Date.valueOf(product.getReleaseDate()));
                productStmt.setString(4, String.valueOf(product.getCountry())); // Поле country теперь VARCHAR(255)
                productStmt.setInt(5, product.getManufacturer().id());
                productStmt.execute();
            }
        }
        log("The table of Product is filled in.");

        log("Filling in the table of Reviews started...");
        try (PreparedStatement reviewStmt = connection.prepareStatement(
                "INSERT INTO Review (date_time, user_name, rating, product_id) VALUES (?, ?, ?, ?)")) {
            for (Product product : products) {
                for (Review review : product.getReviews()) {
                    reviewStmt.setTimestamp(1, java.sql.Timestamp.from(review.getDateTime().toInstant()));
                    reviewStmt.setString(2, review.getUser());
                    reviewStmt.setInt(3, review.getRating());
                    reviewStmt.setInt(4, product.getId());
                    reviewStmt.addBatch();
                }
                reviewStmt.executeBatch();
            }
        }
        log("The table of Reviews is filled in.");

        log("Tables filled with data.");
    }

    public static void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

