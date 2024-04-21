package dao;

import database.DBConnection;
import model.entity.User;
import model.entity.Order;
import model.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static constants.DBConstants.*;

/**
 * Класс OrderDao обеспечивает доступ к данным для объектов Order.
 * Он реализует методы для извлечения и сохранения заказов в базе данных.
 */
public class OrderDao extends DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    /**
     * Получает заказ по его идентификатору из базы данных.
     *
     * @param id Идентификатор заказа для поиска.
     * @return Order объект заказа или null, если заказ не найден.
     * @throws SQLException           если произошла ошибка в запросе к базе данных.
     * @throws IOException            если возникают ошибки ввода/вывода.
     * @throws ClassNotFoundException если не найден драйвер JDBC.
     */
    public Order getOrderById(int id)
            throws SQLException, IOException, ClassNotFoundException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (!resultSet.next()) {
            return null; // Ранний выход, если заказ не найден
        }
        return createOrderFromResultSet(resultSet, id);
    }

    /**
     * Создает объект Order из данных ResultSet.
     *
     * @param resultSet Результат запроса, содержащий данные заказа.
     * @param orderId   Идентификатор заказа.
     * @return Order сформированный объект заказа.
     * @throws SQLException           если произошла ошибка при работе с ResultSet.
     * @throws IOException            если возникают ошибки ввода/вывода.
     * @throws ClassNotFoundException если не найден драйвер JDBC.
     */
    private Order createOrderFromResultSet(ResultSet resultSet, int orderId)
            throws SQLException, IOException, ClassNotFoundException {
        int userId = resultSet.getInt(USER_ID_COLUMN);
        User user = new UserDao().getUserById(userId); // Получение пользователя
        List<Product> products = getProductsForOrder(orderId); // Получение списка продуктов
        Order order = new Order(orderId);
        order.setUser(user);
        order.setProducts(products);
        return order;
    }

    /**
     * Получает список продуктов, входящих в заказ.
     *
     * @param orderId Идентификатор заказа.
     * @return List список продуктов заказа.
     * @throws SQLException           если произошла ошибка SQL.
     * @throws ClassNotFoundException если не найден драйвер JDBC.
     */
    private List<Product> getProductsForOrder(int orderId)
            throws SQLException, ClassNotFoundException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.* FROM products p INNER JOIN order_products op ON p.id = op.product_id WHERE op.order_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                products.add(createProductFromResultSet(resultSet));
            }
        }
        return products;
    }

    /**
     * Создает объект Product из данных ResultSet.
     *
     * @param resultSet Результат запроса, содержащий данные продукта.
     * @return Product сформированный объект продукта.
     * @throws SQLException если произошла ошибка при работе с ResultSet.
     */
    private Product createProductFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt(ID_COLUMN);
        String name = resultSet.getString(NAME_COLUMN);
        double price = resultSet.getDouble(PRICE_COLUMN);
        return new Product(id, name, price);
    }

    /**
     * Добавляет объект заказа в базу данных.
     *
     * @param order Заказ для добавления.
     * @throws SQLException           если произошла ошибка SQL.
     * @throws ClassNotFoundException если не найден драйвер JDBC.
     */
    public void addOrder(Order order)
            throws SQLException, ClassNotFoundException {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            int orderId = insertOrderAndGetId(order, connection);
            linkProductsToOrder(order.getProducts(), orderId, connection);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Вставляет заказ в базу данных и возвращает его идентификатор.
     *
     * @param order      Заказ для вставки.
     * @param connection Активное соединение с базой данных.
     * @return int идентификатор добавленного заказа.
     * @throws SQLException если произошла ошибка SQL.
     */
    private int insertOrderAndGetId(Order order, Connection connection) throws SQLException {
        String insertOrderSql = "INSERT INTO orders (user_id) VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertOrderSql,
                Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, order.getUser().getId());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                logger.error("Failed to insert order, no ID obtained.");
                throw new SQLException();
            }
        }
    }

    /**
     * Связывает продукты с заказом в базе данных.
     *
     * @param products Список продуктов для связывания.
     * @param orderId  Идентификатор заказа.
     * @param conn     Активное соединение с базой данных.
     * @throws SQLException если произошла ошибка SQL.
     */
    private void linkProductsToOrder(List<Product> products, int orderId, Connection conn) throws SQLException {
        String linkProductSql = "INSERT INTO order_products (order_id, product_id) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(linkProductSql)) {
            for (Product product : products) {
                preparedStatement.setInt(1, orderId);
                preparedStatement.setInt(2, product.getId());
                preparedStatement.executeUpdate();
            }
        }
    }

    /**
     * Закрывает соединение с базой данных.
     *
     * @param connection Соединение для закрытия.
     */
    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing connection: ", e);
            }
        }
    }
}
