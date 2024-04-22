package dao;

import database.DBConnection;
import model.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс ProductDao предоставляет методы для управления данными продуктов в базе данных.
 * Он включает операции получения, добавления, обновления и удаления продуктов.
 */
public class ProductDao extends DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(ProductDao.class);

    /**
     * Получает продукт по его идентификатору из базы данных.
     *
     * @param id Идентификатор продукта для поиска.
     * @return Product объект продукта, если он найден, иначе null.
     * @throws SQLException если происходит ошибка SQL при выполнении запроса.
     */
    public Product getProductById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Product(resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getDouble("price"));
                }
            }
        }
        return null;
    }

    /**
     * Получает список всех продуктов из базы данных.
     *
     * @return List список всех продуктов.
     * @throws SQLException если происходит ошибка SQL при выполнении запроса.
     */
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                products.add(new Product(resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price")));
            }
        }
        return products;
    }

    /**
     * Добавляет новый продукт в базу данных.
     *
     * @param product Объект продукта для добавления.
     */
    public void addProduct(Product product) {
        String sql = "INSERT INTO products (name, price) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, product.getName());
            preparedStatement.setDouble(2, product.getPrice());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Не удалось добавить товар: {}", e.getMessage(), e);
        }
    }

    /**
     * Обновляет данные продукта в базе данных.
     *
     * @param product Объект продукта с обновленными данными.
     * @throws SQLException если происходит ошибка SQL при выполнении запроса.
     */
    public void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, price = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, product.getName());
            preparedStatement.setDouble(2, product.getPrice());
            preparedStatement.setInt(3, product.getId());
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Удаляет продукт из базы данных по его идентификатору.
     * Этот метод также удаляет все связанные записи в таблице order_products,
     * чтобы предотвратить нарушение ограничений внешнего ключа.
     *
     * @param id Идентификатор продукта, который нужно удалить.
     * @throws SQLException если происходит ошибка SQL в процессе удаления.
     */
    public void deleteProduct(int id) throws SQLException {
        String sqlDeleteOrderProducts = "DELETE FROM order_products WHERE product_id = ?";
        String sqlDeleteProduct = "DELETE FROM products WHERE id = ?";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement psOrderProducts = connection.prepareStatement(sqlDeleteOrderProducts)) {
                psOrderProducts.setInt(1, id);
                psOrderProducts.executeUpdate();
            }

            try (PreparedStatement psProduct = connection.prepareStatement(sqlDeleteProduct)) {
                psProduct.setInt(1, id);
                psProduct.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try (Connection connection = getConnection()) {
                connection.rollback();
            }
            throw e;
        } finally {
            try (Connection connection = getConnection()) {
                connection.setAutoCommit(true);
            }
        }
    }
}
