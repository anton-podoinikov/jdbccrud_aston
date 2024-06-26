package dao;

import database.ConnectionFactory;
import model.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс UserDao предоставляет доступ к данным пользователей в базе данных.
 * Включает методы для получения, добавления, обновления и удаления пользователей.
 */
public class UserDao {

    /**
     * Получает пользователя по его идентификатору из базы данных.
     *
     * @param id Идентификатор пользователя.
     * @return User объект пользователя, если он найден, иначе null.
     * @throws SQLException при ошибках SQL запросов.
     */
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"));
                }
            }
        }
        return null;
    }

    /**
     * Получает список всех пользователей из базы данных.
     *
     * @return List список всех пользователей.
     * @throws SQLException при ошибках SQL запросов.
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email")));
            }
        }
        return users;
    }

    /**
     * Добавляет нового пользователя в базу данных.
     *
     * @param user Объект пользователя для добавления.
     * @throws SQLException при ошибках SQL запросов.
     */
    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email) VALUES (?, ?)";
        try (Connection connection = ConnectionFactory.getConnection();
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
     * @throws SQLException при ошибках SQL запросов.
     */
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
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
     * @throws SQLException           Если возникла ошибка при выполнении транзакции, включая ошибки обновления данных.
     *                                В таком случае транзакция откатывается.
     */
    public void deleteUser(int id) throws SQLException {
        String sqlDeleteOrderProducts = "DELETE FROM order_products WHERE order_id " +
                "IN (SELECT id FROM orders WHERE user_id = ?)";
        String sqlDeleteOrders = "DELETE FROM orders WHERE user_id = ?";
        String sqlDeleteUser = "DELETE FROM users WHERE id = ?";

        Connection connection = null;
        try {
            connection = ConnectionFactory.getConnection();
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
