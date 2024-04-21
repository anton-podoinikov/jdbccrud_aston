package dao;

import database.DBConnection;
import model.entity.Product;
import util.DatabaseErrorHandler;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static constants.DBConstants.*;

/**
 * Класс ProductDao предоставляет методы для управления данными продуктов в базе данных.
 * Он включает операции получения, добавления, обновления и удаления продуктов.
 */
public class ProductDao extends DBConnection {

    /**
     * Получает продукт по его идентификатору из базы данных.
     *
     * @param id Идентификатор продукта для поиска.
     * @return Product объект продукта, если он найден, иначе null.
     * @throws SQLException           если происходит ошибка SQL при выполнении запроса.
     * @throws IOException            если возникают ошибки ввода/вывода.
     * @throws ClassNotFoundException если класс драйвера JDBC не найден.
     */
    public Product getProductById(int id) throws SQLException, IOException, ClassNotFoundException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Product(resultSet.getInt(ID_COLUMN),
                            resultSet.getString(NAME_COLUMN),
                            resultSet.getDouble(PRICE_COLUMN));
                }
            }
        }
        return null;
    }

    /**
     * Получает список всех продуктов из базы данных.
     *
     * @return List список всех продуктов.
     * @throws SQLException           если происходит ошибка SQL при выполнении запроса.
     * @throws IOException            если возникают ошибки ввода/вывода.
     * @throws ClassNotFoundException если класс драйвера JDBC не найден.
     */
    public List<Product> getAllProducts() throws SQLException, IOException, ClassNotFoundException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                products.add(new Product(resultSet.getInt(ID_COLUMN),
                        resultSet.getString(NAME_COLUMN),
                        resultSet.getDouble(PRICE_COLUMN)));
            }
        }
        return products;
    }

    /**
     * Добавляет новый продукт в базу данных.
     *
     * @param product Объект продукта для добавления.
     */
    public void addProduct(Product product) throws IOException {
        String sql = "INSERT INTO products (name, price) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, product.getName());
            preparedStatement.setDouble(2, product.getPrice());
            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            DatabaseErrorHandler.handleException(e, "Failed to add product");
        }
    }

    /**
     * Обновляет данные продукта в базе данных.
     *
     * @param product Объект продукта с обновленными данными.
     * @throws SQLException           если происходит ошибка SQL при выполнении запроса.
     * @throws IOException            если возникают ошибки ввода/вывода.
     * @throws ClassNotFoundException если класс драйвера JDBC не найден.
     */
    public void updateProduct(Product product) throws SQLException, IOException, ClassNotFoundException {
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
     * @throws SQLException           если происходит ошибка SQL в процессе удаления.
     * @throws IOException            если возникают ошибки ввода/вывода в процессе работы с базой данных.
     * @throws ClassNotFoundException если не найден класс драйвера JDBC.
     */
    public void deleteProduct(int id) throws SQLException, IOException, ClassNotFoundException {
        // Сначала удаляем связанные данные из order_products
        String sqlDeleteOrderProducts = "DELETE FROM order_products WHERE product_id = ?";
        // Затем удаляем сам продукт
        String sqlDeleteProduct = "DELETE FROM products WHERE id = ?";

        try (Connection connection = getConnection()) {
            // Отключаем auto-commit для управления транзакцией
            connection.setAutoCommit(false);

            // Удаление из order_products
            try (PreparedStatement psOrderProducts = connection.prepareStatement(sqlDeleteOrderProducts)) {
                psOrderProducts.setInt(1, id);
                psOrderProducts.executeUpdate();
            }

            // Удаление продукта
            try (PreparedStatement psProduct = connection.prepareStatement(sqlDeleteProduct)) {
                psProduct.setInt(1, id);
                psProduct.executeUpdate();
            }

            // Подтверждение транзакции
            connection.commit();
        } catch (SQLException e) {
            // Откат в случае ошибки
            try (Connection connection = getConnection()) {
                connection.rollback();
            }
            throw e;
        } finally {
            // Возвращаем auto-commit в исходное состояние
            try (Connection connection = getConnection()) {
                connection.setAutoCommit(true);
            }
        }
    }

}
