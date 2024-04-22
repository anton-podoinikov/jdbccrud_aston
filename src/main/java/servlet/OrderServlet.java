package servlet;

import com.google.gson.Gson;
import converter.OrderConverter;
import dao.OrderDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.OrderDto;
import model.entity.Order;

import java.io.IOException;

import static util.ServletUtils.writeResponse;

/**
 * Сервлет, предоставляющий REST API для управления заказами.
 * Поддерживает операции для получения и создания заказов.
 */
@WebServlet("/orders")
public class OrderServlet extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();
    private final OrderConverter orderConverter = new OrderConverter();
    private final Gson gson = new Gson();

    /**
     * Обрабатывает HTTP GET запрос на получение заказа по его ID.
     * Заказ возвращается в виде строки в формате JSON.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, содержащий ответ сервлета клиенту.
     * @throws IOException при возникновении ошибки ввода-вывода во время обработки запроса.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String orderId = request.getParameter("id");
        try {
            Order order = orderId == null ? null : orderDao.getOrderById(Integer.parseInt(orderId));
            if (order != null) {
                OrderDto orderDto = orderConverter.convertEntityToDto(order);
                writeResponse(response, gson.toJson(orderDto), HttpServletResponse.SC_OK);
            } else {
                writeResponse(response, "Заказ не найден", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            writeResponse(response, "Внутренняя ошибка сервера", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Обрабатывает HTTP POST запрос на создание нового заказа из JSON-форматированного DTO заказа.
     * Метод читает DTO заказа, преобразует его в сущность и сохраняет в базе данных.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, который содержит ответ сервлета на запрос клиента.
     * @throws IOException при обнаружении ошибки ввода или вывода при обработке запроса.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            OrderDto orderDto = gson.fromJson(request.getReader(), OrderDto.class);
            Order order = orderConverter.convertDTOToEntity(orderDto);
            orderDao.addOrder(order);
            writeResponse(response, "Заказ успешно создан", HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            writeResponse(response, "Ошибка обработки запроса: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
