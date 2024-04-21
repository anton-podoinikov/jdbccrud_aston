package dao;

import model.entity.User;
import database.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static constants.DBConstants.*;

/**
 * Класс UserDao предоставляет доступ к данным пользователей в базе данных.
 * Включает методы для получения, добавления, обновления и удаления пользователей.
 */
public class UserDao extends DBConnection {

    /**
     * Получает пользователя по его идентификатору из базы данных.
     *
     * @param id Идентификатор пользователя.
     * @return User объект пользователя, если он найден, иначе null.
     * @throws SQLException           при ошибках SQL запросов.
     * @throws IOException            при ошибках ввода/вывода.
     * @throws ClassNotFoundException если не найден драйвер JDBC.
     */
    public User getUserById(int id) throws SQLException, IOException, ClassNotFoundException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(resultSet.getInt(ID_COLUMN),
                            resultSet.getString(USER_NAME_COLUMN),
                            resultSet.getString(EMAIL_NAME_COLUMN));
                }
            }
        }
        return null;
    }

    /**
     * Получает список всех пользователей из базы данных.
     *
     * @return List список всех пользователей.
     * @throws SQLException           при ошибках SQL запросов.
     * @throws IOException            при ошибках ввода/вывода.
     * @throws ClassNotFoundException если не найден драйвер JDBC.
     */
    public List<User> getAllUsers() throws SQLException, IOException, ClassNotFoundException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                users.add(new User(resultSet.getInt(ID_COLUMN),
                        resultSet.getString(USER_NAME_COLUMN),
                        resultSet.getString(EMAIL_NAME_COLUMN)));
            }
        }
        return users;
    }

    /**
     * Добавляет нового пользователя в базу данных.
     *
     * @param user Объект пользователя для добавления.
     * @throws SQLException           при ошибках SQL запросов.
     * @throws IOException            при ошибках ввода/вывода.
     * @throws ClassNotFoundException если не найден драйвер JDBC.
     */
    public void addUser(User user) throws SQLException, IOException, ClassNotFoundException {
        String sql = "INSERT INTO users (username, email) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Обновляет данные пользователя в базе данных.
     *
     * @param user Объект пользователя с обновленными данными.
     * @throws SQLException           при ошибках SQL запросов.
     * @throws IOException            при ошибках ввода/вывода.
     * @throws ClassNotFoundException если не найден драйвер JDBC.
     */
    public void updateUser(User user) throws SQLException, IOException, ClassNotFoundException {
        String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setInt(3, user.getId());
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Удаляет пользователя и все связанные с ним записи из базы данных.
     * Этот метод сначала удаляет все продукты, связанные с заказами пользователя,
     * затем удаляет сами заказы пользователя, и в конце удаляет самого пользователя.
     * Все операции выполняются в рамках одной транзакции.
     *
     * @param id Идентификатор пользователя, которого нужно удалить.
     * @throws SQLException           Если происходит ошибка SQL при выполнении запросов.
     *                                Это может быть связано с нарушением целостности данных,
     *                                например, когда пытаемся удалить пользователя, который уже используется в других таблицах.
     * @throws IOException            Если возникают ошибки ввода/вывода.
     * @throws ClassNotFoundException Если не найден драйвер JDBC.
     * @throws SQLException           Если возникла ошибка при выполнении транзакции, включая ошибки обновления данных.
     *                                В таком случае транзакция откатывается.
     */
    public void deleteUser(int id) throws SQLException, IOException, ClassNotFoundException {
        String sqlDeleteOrderProducts = "DELETE FROM order_products WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?)";
        String sqlDeleteOrders = "DELETE FROM orders WHERE user_id = ?";
        String sqlDeleteUser = "DELETE FROM users WHERE id = ?";

        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement psOrderProducts = connection.prepareStatement(sqlDeleteOrderProducts)) {
                psOrderProducts.setInt(1, id);
                psOrderProducts.executeUpdate();
            }

            try (PreparedStatement psOrders = connection.prepareStatement(sqlDeleteOrders)) {
                psOrders.setInt(1, id);
                psOrders.executeUpdate();
            }

            try (PreparedStatement psUser = connection.prepareStatement(sqlDeleteUser)) {
                psUser.setInt(1, id);
                psUser.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

}
