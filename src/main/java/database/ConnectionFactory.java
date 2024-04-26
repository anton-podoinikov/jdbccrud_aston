package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private static String url;
    private static String username;
    private static String password;
    private static boolean isEnvironment = false;

    public static void configureEnvironment(String url, String username, String password) {
        ConnectionFactory.url = url;
        ConnectionFactory.username = username;
        ConnectionFactory.password = password;
        isEnvironment = true;
    }

    public static void clearEnvironment() {
        isEnvironment = false;
    }

    public static Connection getConnection() {
        if (isEnvironment) {
            try {
                return DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            String url = ConfigLoader.getProperty("database.url");
            String username = ConfigLoader.getProperty("database.username");
            String password = ConfigLoader.getProperty("database.password");
            try {
                return DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
