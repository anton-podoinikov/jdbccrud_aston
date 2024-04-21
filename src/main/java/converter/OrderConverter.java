package converter;

import dao.ProductDao;
import dao.UserDao;
import model.dto.OrderDto;
import model.dto.ProductDto;
import model.dto.UserDto;
import model.entity.Order;
import model.entity.Product;
import model.entity.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
     * @throws IOException              при ошибках ввода/вывода.
     * @throws ClassNotFoundException   если класс для JDBC драйвера не найден.
     * @throws IllegalStateException    если не найден пользователь или продукт.
     * @throws IllegalArgumentException если DTO не содержит продуктов.
     */
    public Order convertDTOToEntity(OrderDto orderDto) throws SQLException, IOException, ClassNotFoundException {
        UserDao userDao = new UserDao();
        ProductDao productDao = new ProductDao();

        User user = userDao.getUserById(orderDto.getUser().getId());
        if (user == null) {
            throw new IllegalStateException("User with ID " + orderDto.getUser().getId() + " not found");
        }

        List<Product> products = new ArrayList<>();
        if (orderDto.getProducts() == null || orderDto.getProducts().isEmpty()) {
            throw new IllegalArgumentException("An order must have at least one product.");
        }
        for (ProductDto productDto : orderDto.getProducts()) {
            Product product = productDao.getProductById(productDto.getId());
            if (product == null) {
                throw new IllegalStateException("Product with ID " + productDto.getId() + " not found");
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
        List<ProductDto> productDtos = order.getProducts().stream()
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice()))
                .toList();
        UserDto userDto = new UserDto(order.getUser().getId(), order.getUser().getUsername(), order.getUser().getEmail());
        return new OrderDto(order.getId(), userDto, productDtos);
    }
}
