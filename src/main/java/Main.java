import database.DatabaseManager;
import org.openjdk.jmh.runner.RunnerException;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        DatabaseManager dbmanager = new DatabaseManager();
        try {
            dbmanager.setupSshTunnel();
            log("SSH tunnel established.");

            try (Connection connection = dbmanager.connect()) {
                log("Connected to the database.");

                dbmanager.dropTables(connection);
                dbmanager.createTables(connection);

                int manufacturerCount = 10;
                int productsCount = 10000;
                int reviewsPerProduct = 5;

                dbmanager.fillTables(connection, manufacturerCount, productsCount, reviewsPerProduct);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Закрыть SSH-туннель
            dbmanager.closeSshTunnel();
        }
    }
    public static void log(String message) {
        System.out.println("[LOG] " + message);
    }
}
