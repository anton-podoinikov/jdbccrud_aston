package dao;

import database.ConnectionFactory;
import model.entity.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Класс для интеграционного тестирования UserDao, использующий Testcontainers для создания
 * изолированной тестовой базы данных PostgreSQL.
 * Осуществляет тестирование основных CRUD-операций для управления пользователями.
 */
@Testcontainers
public class UserDaoTest {

    private UserDao userDao = new UserDao();

    /**
     * Контейнер PostgreSQL, настроенный для использования в тестах.
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
     * Тестирует получение пользователя по идентификатору.
     * Проверяет, что данные пользователя корректно извлекаются из базы данных.
     */
    @Test
    void testGetUserByIdExisting() throws Exception {
        User user = userDao.getUserById(2);
        assertNotNull(user);
        assertEquals("Oleg", user.getUsername());
        assertEquals("oleg@mail.com", user.getEmail());
    }

    /**
     * Тестирует добавление нового пользователя в базу данных.
     * Проверяет, что пользователь добавлен и существует в списке всех пользователей.
     */

    @Test
    void testAddUser() throws Exception {
        User newUser = new User("New User", "newuser@example.com");
        userDao.addUser(newUser);
        List<User> users = userDao.getAllUsers();
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("newuser@example.com")));
    }

    /**
     * Тестирует получение списка всех пользователей.
     * Проверяет, что возвращается корректное количество пользователей, включая проверку наличия конкретного пользователя.
     */
    @Test
    void testGetAllUsers() throws Exception {
        List<User> users = userDao.getAllUsers();
        assertEquals(5, users.size()); // Check that all pre-inserted users are retrieved
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("Vasya")));
    }

    /**
     * Тестирует обновление данных существующего пользователя.
     * Проверяет, что данные пользователя успешно обновлены в базе данных.
     */
    @Test
    void testUpdateUser() throws Exception {
        User user = userDao.getUserById(1);
        user.setUsername("Обновленный Anton");
        userDao.updateUser(user);
        User updatedUser = userDao.getUserById(1);
        assertEquals("Обновленный Anton", updatedUser.getUsername());
    }

    /**
     * Тестирует удаление пользователя по идентификатору.
     * Проверяет, что пользователь был удален из базы данных, и обновленный список пользователей соответствует ожиданиям.
     */
    @Test
    void testDeleteUser() throws Exception {
        userDao.deleteUser(5);
        assertNull(userDao.getUserById(5));
        List<User> users = userDao.getAllUsers();
        assertEquals(4, users.size()); // Verify one less user after deletion
    }
}
