package database;

import org.junit.jupiter.api.*;
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
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.sql");

    @BeforeAll
    public static void setupDatabaseConnection() {
        postgres.start();
        ConnectionFactory.configureEnvironment(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    @AfterAll
    public static void tearDownDatabaseConnection() {
        postgres.stop();
        ConnectionFactory.clearEnvironment();
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
        try (Connection connection = ConnectionFactory.getConnection()) {
            assertNotNull(connection, "Подключение к базе данных не должно быть null");
            assertFalse(connection.isClosed(), "Подключение к базе данных не должно быть закрыто");
        }
    }
}
