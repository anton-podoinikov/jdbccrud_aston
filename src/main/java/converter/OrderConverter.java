package converter;

import dao.ProductDao;
import dao.UserDao;
import model.dto.OrderDto;
import model.entity.Order;
import model.entity.Product;
import model.entity.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс OrderConverter предоставляет функциональность для конвертации
 * между объектами OrderDto и Order. Это включает в себя преобразование
 * данных из формата, пригодного для передачи данных (DTO), в сущности
 * базы данных и обратно.
 */
public class OrderConverter {

    /**
     * Преобразует OrderDto в сущность Order.
     * Извлекает пользователя и продукты из базы данных по ID, указанным в OrderDto,
     * и формирует из них объект Order.
     *
     * @param orderDto DTO заказа, который нужно преобразовать в сущность.
     * @return сущность Order, соответствующая предоставленному DTO.
     * @throws SQLException             при ошибках доступа к базе данных.
     * @throws IllegalStateException    если не найден пользователь или продукт.
     * @throws IllegalArgumentException если DTO не содержит продуктов.
     */
    public Order convertDTOToEntity(OrderDto orderDto) throws SQLException {
        UserDao userDao = new UserDao();
        ProductDao productDao = new ProductDao();

        User user = userDao.getUserById(orderDto.getUserId());
        if (user == null) {
            throw new IllegalStateException("Пользователь с идентификатором " + orderDto.getUserId() + " не найден");
        }

        List<Product> products = new ArrayList<>();
        if (orderDto.getProductIds() == null || orderDto.getProductIds().isEmpty()) {
            throw new IllegalArgumentException("В заказе должен быть хотя бы один товар.");
        }
        for (Integer productId : orderDto.getProductIds()) {
            Product product = productDao.getProductById(productId);
            if (product == null) {
                throw new IllegalStateException("Продукт с идентификатором " + productId + " не найден");
            }
            products.add(product);
        }

        Order order = new Order();
        order.setUser(user);
        order.setProducts(products);
        return order;
    }


    /**
     * Конвертирует сущность Order в OrderDto.
     * Использует данные о заказе для создания DTO, который содержит базовую
     * информацию о заказе, а также детализированную информацию о каждом продукте.
     *
     * @param order сущность заказа, которую нужно преобразовать в DTO.
     * @return DTO заказа, содержащий данные из сущности.
     */
    public OrderDto convertEntityToDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setUserId(order.getUser().getId());
        List<OrderDto.ProductInfo> productInfos = order.getProducts().stream()
                .map(p -> new OrderDto.ProductInfo(p.getId(), p.getName(), p.getPrice()))
                .collect(Collectors.toList());
        orderDto.setProducts(productInfos);
        return orderDto;
    }
}
