package daotest;

import dao.OrderDao;
import model.entity.Order;
import model.entity.Product;
import model.entity.User;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Класс для интеграционного тестирования OrderDao с использованием Testcontainers и PostgreSQL.
 * Тесты включают проверку функций получения и добавления заказов в базу данных.
 */
@Testcontainers
public class OrderDaoTest {

    /**
     * Контейнер PostgreSQL, который используется для создания изолированной тестовой базы данных.
     */
    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    /**
     * Настройка тестовой среды перед каждым тестом, включая миграцию базы данных.
     */
    @BeforeEach
    void setUp() {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db.migration")
                .cleanDisabled(false)
                .load();
        flyway.clean();
        flyway.migrate();

        System.setProperty("database.url", postgres.getJdbcUrl());
        System.setProperty("database.username", postgres.getUsername());
        System.setProperty("database.password", postgres.getPassword());
        System.setProperty("database.driver", "org.postgresql.Driver");
    }

    /**
     * Тестирование метода getOrderById для проверки возможности получения существующего заказа по идентификатору.
     */
    @Test
    void testGetOrderById() throws Exception {
        OrderDao orderDao = new OrderDao();
        Order order = orderDao.getOrderById(1);
        assertNotNull(order, "Заказ должен быть найден");
    }

    /**
     * Тестирование метода addOrder для проверки добавления нового заказа и связывания продуктов с этим заказом.
     */
    @Test
    void testAddOrder() throws Exception {
        int initialProductCount = getProductCountInOrder(postgres);

        User user = new User(1, "Anton", "antpkov@gmail.com");
        Product product1 = new Product(1, "Кола", 1.50);
        Product product2 = new Product(2, "Молоко", 5.50);

        Order order = new Order();
        order.setUser(user);
        order.setProducts(Arrays.asList(product1, product2));

        OrderDao orderDao = new OrderDao();
        orderDao.addOrder(order);

        int finalProductCount = getProductCountInOrder(postgres);
        assertEquals(initialProductCount + 2, finalProductCount, "Должно быть связано два продукта с заказом.");
    }

    /**
     * Вспомогательный метод для подсчета количества связей продуктов с заказами в базе данных.
     */
    private int getProductCountInOrder(PostgreSQLContainer<?> postgres) throws SQLException {
        try (Connection connection = postgres.createConnection("");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM order_products")) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }
}
