package daotest;

import dao.ProductDao;
import model.entity.Product;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    private ProductDao productDao;

    /**
     * Контейнер для PostgreSQL, настроенный для тестирования.
     */
    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("postgres")
            .withPassword("postgres");

    /**
     * Подготовка тестовой среды, включая миграцию базы данных и установку системных свойств для доступа к БД.
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

        productDao = new ProductDao();

        System.setProperty("database.url", postgres.getJdbcUrl());
        System.setProperty("database.username", postgres.getUsername());
        System.setProperty("database.password", postgres.getPassword());
        System.setProperty("database.driver", "org.postgresql.Driver");
    }

    /**
     * Тестирование получения продукта по ID. Проверяет, что продукт успешно извлекается из базы данных.
     */
    @Test
    void testGetProductById() throws Exception {
        productDao = new ProductDao();
        Product expected = productDao.getProductById(1); // Используем предустановленный продукт
        assertNotNull(expected, "Product should not be null");
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
        Assertions.assertFalse(products.isEmpty()); // Проверка, что в базе уже есть продукты
        assertTrue(products.size() >= 12); // Количество начальных продуктов
    }

    /**
     * Тестирование добавления нового продукта в базу данных.
     * Проверяет, что продукт был успешно добавлен.
     */
    @Test
     void testAddProduct() throws Exception {
        Product newProduct = new Product("Новый продукт", 10.99);
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
        Product product = productDao.getProductById(1); // Получаем существующий продукт
        product.setName("Обновленная Кола");
        product.setPrice(2.00);
        productDao.updateProduct(product);
        Product updatedProduct = productDao.getProductById(1);
        assertEquals("Обновленная Кола", updatedProduct.getName());
        assertEquals(2.00, updatedProduct.getPrice(), 0.01);
    }

    /**
     * Тестирование удаления продукта по ID.
     * Проверяет, что продукт был успешно удален из базы данных.
     */
    @Test
     void testDeleteProduct() throws Exception {
        int productIdToDelete = 5; // Идентификатор продукта, который существует
        productDao.deleteProduct(productIdToDelete);
        assertNull(productDao.getProductById(productIdToDelete));
    }
}
