package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    public Connection getConnection() {
        String url = ConfigLoader.getProperty("database.url");
        String username = ConfigLoader.getProperty("database.username");
        String password = ConfigLoader.getProperty("database.password");
        String driver = ConfigLoader.getProperty("database.driver");

        if (driver == null) {
            throw new IllegalStateException("Драйвер не установлен. Проверьте свойства или конфигурации вашей системы.");
        }

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            logger.error("Ошибка загрузки драйвера JDBC: " + driver, e);
            throw new RuntimeException(e);
        }

        if (url == null || username == null || password == null) {
            throw new IllegalStateException("Параметры подключения к базе данных заданы не полностью. (url/username/password).");
        }

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            logger.error("Ошибка при подключении к базе данных: ", e);
            throw new RuntimeException(e);
        }
    }
}
