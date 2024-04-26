package servlet;

import static org.mockito.Mockito.*;

import com.google.gson.Gson;
import converter.OrderConverter;
import dao.OrderDao;
import jakarta.servlet.http.*;
import model.dto.OrderDto;
import model.entity.Order;
import model.entity.Product;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Тестовый класс для {@link OrderServlet}, обеспечивающий проверку функциональности обработки заказов.
 * В тестах используются заглушки для зависимостей сервлета, такие как {@link OrderDao}, {@link OrderConverter}, и {@link Gson},
 * чтобы проверить логику сервлета изолированно от внешних сервисов.
 */
public class OrderServletTest {
    private OrderServlet servlet;
    private OrderDao mockOrderDao;
    private OrderConverter mockOrderConverter;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private PrintWriter mockPrintWriter;
    private Gson mockGson;

    /**
     * Настраивает тестовую среду перед каждым тестом, создавая и конфигурируя заглушки для всех необходимых зависимостей.
     * Также производит инъекцию заглушек через рефлексию в приватные поля сервлета.
     * @throws Exception если возникает ошибка рефлексии при доступе к полям сервлета
     */
    @BeforeEach
    public void setUp() throws Exception {
        mockOrderDao = mock(OrderDao.class);
        mockOrderConverter = mock(OrderConverter.class);
        mockGson = mock(Gson.class);

        servlet = new OrderServlet();

        Field daoField = OrderServlet.class.getDeclaredField("orderDao");
        daoField.setAccessible(true);
        daoField.set(servlet, mockOrderDao);

        Field converterField = OrderServlet.class.getDeclaredField("orderConverter");
        converterField.setAccessible(true);
        converterField.set(servlet, mockOrderConverter);

        Field gsonField = OrderServlet.class.getDeclaredField("gson");
        gsonField.setAccessible(true);
        gsonField.set(servlet, mockGson);

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockPrintWriter = mock(PrintWriter.class);

        when(mockResponse.getWriter()).thenReturn(mockPrintWriter);
    }

    /**
     * Тестирование метода {@link OrderServlet#doGet(HttpServletRequest, HttpServletResponse)}.
     * Проверяет, что при передаче идентификатора существующего заказа, сервлет корректно получает заказ,
     * преобразует его в DTO, сериализует в JSON и отправляет в ответе клиенту.
     * @throws IOException если возникают ошибки ввода-вывода
     */
    @Test
    public void testDoGetWithExistingOrder() throws IOException {
        when(mockRequest.getParameter("id")).thenReturn("1");
        Order order = new Order(1);
        User user = new User(1, "Test User", "test@example.com");
        order.setUser(user);
        List<Product> products = new ArrayList<>();
        products.add(new Product(1, "Test Product", 100.00));
        order.setProducts(products);
        try {
            when(mockOrderDao.getOrderById(1)).thenReturn(order);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        OrderDto orderDto = new OrderDto();
        when(mockOrderConverter.convertEntityToDto(order)).thenReturn(orderDto);
        String jsonResponse = "{\"id\":1,\"userId\":1,\"products\":[{\"productId\":1,\"name\":\"Test Product\",\"price\":100.00}]}";
        when(mockGson.toJson(orderDto)).thenReturn(jsonResponse);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockPrintWriter).println(jsonResponse);
        verify(mockPrintWriter).flush();
    }

    /**
     * Тестирование метода {@link OrderServlet#doPost(HttpServletRequest, HttpServletResponse)}.
     * Проверяет, что сервлет корректно обрабатывает POST-запрос на создание нового заказа.
     * Данные заказа читаются из тела запроса, конвертируются в объект {@link Order}, и регистрируется новый заказ.
     * @throws IOException если возникают ошибки ввода-вывода
     */
    @Test
    public void testDoPostCreatesOrder() throws IOException {
        String jsonInput = "{\"id\":0,\"userId\":2,\"products\":[]}";
        OrderDto orderDto = new OrderDto();
        when(mockGson.fromJson(any(Reader.class), eq(OrderDto.class))).thenReturn(orderDto);

        BufferedReader reader = new BufferedReader(new StringReader(jsonInput));
        when(mockRequest.getReader()).thenReturn(reader);

        Order order = new Order();
        try {
            when(mockOrderConverter.convertDTOToEntity(orderDto)).thenReturn(order);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        servlet.doPost(mockRequest, mockResponse);

        verify(mockPrintWriter).println("Заказ успешно создан");
        verify(mockPrintWriter).flush();
    }
}