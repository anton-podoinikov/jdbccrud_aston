package dao;

import database.ConnectionFactory;
import model.entity.Product;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Класс тестирования ProductDao с использованием Testcontainers для интеграционного тестирования.
 * Проверяет CRUD операции для продуктов в базе данных.
 */
@Testcontainers
public class ProductDaoTest {

    private ProductDao productDao = new ProductDao();

    /**
     * Контейнер PostgreSQL, который используется для создания изолированной тестовой базы данных.
     */
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
     * Тестирование получения продукта по ID. Проверяет, что продукт успешно извлекается из базы данных.
     */
    @Test
    void testGetProductById() throws Exception {
        Product expected = productDao.getProductById(1);
        assertNotNull(expected, "Товар не должен быть нулевым");
        assertEquals("Кола", expected.getName());
        assertEquals(1.50, expected.getPrice(), 0.01);
    }

    /**
     * Тестирование получения всех продуктов из базы данных.
     * Проверяет, что в базе данных существует ожидаемое количество продуктов.
     */
    @Test
     void testGetAllProducts() throws Exception {
        List<Product> products = productDao.getAllProducts();
        Assertions.assertFalse(products.isEmpty());
        assertTrue(products.size() >= 12);
    }

    /**
     * Тестирование добавления нового продукта в базу данных.
     * Проверяет, что продукт был успешно добавлен.
     */
    @Test
     void testAddProduct() throws Exception {
        Product newProduct = new Product(13,"Новый продукт", 10.99);
        productDao.addProduct(newProduct);
        List<Product> products = productDao.getAllProducts();
        assertTrue(products.stream().anyMatch(p -> p.getName().equals("Новый продукт")));
    }

    /**
     * Тестирование обновления существующего продукта.
     * Проверяет, что данные продукта были обновлены корректно.
     */
    @Test
     void testUpdateProduct() throws Exception {
        Product product = productDao.getProductById(2);
        product.setName("Обновленное Молоко");
        product.setPrice(2.00);
        productDao.updateProduct(product);
        Product updatedProduct = productDao.getProductById(2);
        assertEquals("Обновленное Молоко", updatedProduct.getName());
        assertEquals(2.00, updatedProduct.getPrice(), 0.01);
    }

    /**
     * Тестирование удаления продукта по ID.
     * Проверяет, что продукт был успешно удален из базы данных.
     */
    @Test
     void testDeleteProduct() throws Exception {
        int productIdToDelete = 5;
        productDao.deleteProduct(productIdToDelete);
        assertNull(productDao.getProductById(productIdToDelete));
    }
}
