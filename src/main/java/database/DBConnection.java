package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    /**
     * Создает и возвращает подключение к базе данных.
     * Этот метод загружает конфигурационные параметры из файла свойств,
     * инициализирует JDBC драйвер и создает подключение к базе данных.
     *
     * @return {@link Connection} объект, представляющий подключение к базе данных.
     * @throws SQLException           если происходит ошибка SQL при создании подключения к базе данных.
     * @throws ClassNotFoundException если JDBC драйвер не найден.
     */
    public Connection getConnection() throws SQLException, ClassNotFoundException {
        String url = System.getProperty("database.url");
        String username = System.getProperty("database.username");
        String password = System.getProperty("database.password");
        String driver = System.getProperty("database.driver");

        if (driver == null) {
            throw new IllegalStateException("Драйвер не установлен. Проверьте свойства или конфигурации вашей системы.");
        }

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            logger.error("Ошибка загрузки драйвера JDBC.: " + driver);
            throw e;
        }

        if (url == null || username == null || password == null) {
            throw new IllegalStateException("Параметры подключения к базе данных заданы не полностью. (url/username/password).");
        }

        return DriverManager.getConnection(url, username, password);
    }
}
