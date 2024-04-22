package databasetest;

import database.DBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Класс для интеграционного тестирования метода getConnection класса DBConnection.
 * Использует контейнеры Testcontainers для создания временной тестовой среды с базой данных PostgreSQL.
 * Тестирует функциональность создания и проверки подключения к базе данных.
 */
@Testcontainers
public class DBConnectionIntegrationTest {
    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    private DBConnection dbConnection;

    /**
     * Настройка тестового окружения перед каждым тестом. Устанавливает системные свойства, необходимые для конфигурации
     * подключения к базе данных.
     */
    @BeforeEach
    void setUp() {
        // Настройка системных свойств
        System.setProperty("database.url", postgres.getJdbcUrl());
        System.setProperty("database.username", postgres.getUsername());
        System.setProperty("database.password", postgres.getPassword());
        System.setProperty("database.driver", "org.postgresql.Driver");

        dbConnection = new DBConnection();
    }

    /**
     * Очистка установленных системных свойств после каждого теста, чтобы обеспечить изоляцию тестов.
     */
    @AfterEach
    void tearDown() {
        System.clearProperty("database.url");
        System.clearProperty("database.username");
        System.clearProperty("database.password");
        System.clearProperty("database.driver");
    }

    /**
     * Тестирование успешного получения подключения к базе данных.
     * Проверяет, что подключение успешно создается и что оно активно (не закрыто).
     */
    @Test
    void getConnection_Success() throws Exception {
        // Проверка получения подключения
        try (Connection connection = dbConnection.getConnection()) {
            assertNotNull(connection, "Подключение к базе данных не должно быть null");
            assertFalse(connection.isClosed(), "Подключение к базе данных не должно быть закрыто");
        }
    }
}
