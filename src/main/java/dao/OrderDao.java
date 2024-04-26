package dao;

import database.ConnectionFactory;
import model.entity.User;
import model.entity.Order;
import model.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс OrderDao обеспечивает доступ к данным заказов в базе данных.
 * Он реализует методы для извлечения, добавления и обработки заказов.
 */
public class OrderDao {

    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    /**
     * Получает заказ по идентификатору.
     *
     * @param id Идентификатор заказа.
     * @return Order объект заказа или null, если заказ не найден.
     * @throws SQLException в случае ошибок SQL.
     */
    public Order getOrderById(int id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;  // Если заказ не найден, возвращаем null.
                }
                return createOrderFromResultSet(resultSet);
            }
        }
    }

    /**
     * Создаёт объект Order из данных ResultSet.
     *
     * @param resultSet Результат запроса SQL.
     * @return сформированный объект Order.
     * @throws SQLException при ошибках обработки запроса.
     */
    private Order createOrderFromResultSet(ResultSet resultSet) throws SQLException {
        int orderId = resultSet.getInt("id");
        User user = new UserDao().getUserById(resultSet.getInt("user_id"));
        List<Product> products = getProductsForOrder(orderId);
        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setProducts(products);
        return order;
    }

    /**
     * Получает список продуктов для заказа.
     *
     * @param orderId Идентификатор заказа.
     * @return Список продуктов.
     * @throws SQLException при ошибках SQL.
     */
    private List<Product> getProductsForOrder(int orderId) throws SQLException {
        String sql = "SELECT p.* FROM products p INNER JOIN order_products op " +
                "ON p.id = op.product_id WHERE op.order_id = ?";
        List<Product> products = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, orderId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(new Product(resultSet.getInt("id"), resultSet.getString("name"),
                            resultSet.getDouble("price")));
                }
            }
        }
        return products;
    }

    /**
     * Добавляет заказ в базу данных.
     *
     * @param order Заказ для добавления.
     * @throws SQLException при ошибках SQL.
     */
    public void addOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (user_id) VALUES (?)";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, order.getUser().getId());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Не удалось создать заказ, ни одна строка не затронута.");
            }
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    linkProductsToOrder(order.getProducts(), orderId, connection);
                } else {
                    logger.error("Не удалось создать заказ, идентификатор не получен.");
                    throw new SQLException();
                }
            }
        }
    }

    /**
     * Связывает продукты с заказом в базе данных.
     *
     * @param products   Список продуктов.
     * @param orderId    Идентификатор заказа.
     * @param connection Соединение с базой данных.
     * @throws SQLException при ошибках SQL.
     */
    private void linkProductsToOrder(List<Product> products, int orderId,
                                     Connection connection) throws SQLException {
        String sql = "INSERT INTO order_products (order_id, product_id) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (Product product : products) {
                preparedStatement.setInt(1, orderId);
                preparedStatement.setInt(2, product.getId());
                preparedStatement.executeUpdate();
            }
        }
    }
}
