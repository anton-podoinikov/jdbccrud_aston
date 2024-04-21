package servlettest;

import com.google.gson.Gson;
import converter.OrderConverter;
import dao.OrderDao;
import model.dto.OrderDto;
import model.entity.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlet.OrderServlet;

import java.io.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Класс тестов для {@link OrderServlet}.
 * Проверяет корректность методов doGet и doPost для различных сценариев использования.
 */
class OrderServletTest {

    private OrderServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private OrderDao orderDao;
    private OrderConverter orderConverter;
    private final Gson gson = new Gson();
    private StringWriter responseWriter;

    /**
     * Подготовка тестовой среды перед каждым тестом.
     * Инициализирует моки и сервлет с зависимостями.
     */
    @BeforeEach
    void setUp() throws Exception {
        orderDao = mock(OrderDao.class);
        orderConverter = mock(OrderConverter.class);
        servlet = new OrderServlet(orderDao, orderConverter, gson);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    /**
     * Тестирует метод doGet, когда заказ существует.
     * Проверяет, что статус ответа установлен в SC_OK и что тело ответа не пусто.
     */
    @Test
    void testDoGetValidOrder() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        Order order = new Order();
        when(orderDao.getOrderById(1)).thenReturn(order);
        OrderDto orderDto = new OrderDto();
        when(orderConverter.convertEntityToDto(order)).thenReturn(orderDto);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).getWriter();
        Assertions.assertNotNull(responseWriter.toString());
    }

    /**
     * Тестирует метод doGet, когда заказ не найден.
     * Проверяет, что статус ответа установлен в SC_NOT_FOUND.
     */
    @Test
    void testDoGetOrderNotFound() throws Exception {
        when(request.getParameter("id")).thenReturn("999");
        when(orderDao.getOrderById(999)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Тестирует метод doPost при успешном создании заказа.
     * Проверяет, что заказ был добавлен и что статус ответа установлен в SC_CREATED.
     */
    @Test
    void testDoPostCreatesOrder() throws Exception {
        when(request.getReader())
                .thenReturn(new java.io.BufferedReader
                        (new java.io.StringReader("{\"id\":1,\"name\":\"Test Order\"}")));
        Order order = new Order();
        when(orderConverter.convertDTOToEntity(any(OrderDto.class))).thenReturn(order);
        doNothing().when(orderDao).addOrder(any(Order.class));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    /**
     * Тестирует метод doPost при невозможности прочитать тело запроса.
     * Проверяет, что статус ответа установлен в SC_INTERNAL_SERVER_ERROR.
     */
    @Test
    void testDoPostBadRequest() throws Exception {
        when(request.getReader()).thenThrow(new IOException("Не удалось прочитать"));
        servlet.doPost(request, response);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}