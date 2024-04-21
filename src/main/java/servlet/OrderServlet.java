package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import converter.OrderConverter;
import dao.OrderDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import model.dto.OrderDto;
import model.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

import static constants.ServletConstants.*;
import static util.ServletUtils.writeResponse;

/**
 * Сервлет предоставляет REST API для управления заказами в системе.
 * Он поддерживает получение заказа по ID и создание нового заказа.
 * Реализация основана на использовании JSON для обмена данными.
 */
@WebServlet("/orders")
@AllArgsConstructor
public class OrderServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(OrderServlet.class);

    private OrderDao orderDao;
    private OrderConverter orderConverter;
    private Gson gson;


    /**
     * Обрабатывает HTTP GET запросы. Извлекает параметр 'id' из запроса и возвращает заказ
     * в формате JSON, если заказ найден. В случае отсутствия параметра 'id' или если заказ не найден,
     * возвращает соответствующее сообщение об ошибке.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, предоставляющий API для изменения ответа.
     * @throws IOException если происходит ошибка ввода-вывода.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String orderId = request.getParameter("id");
        if (orderId == null || orderId.isEmpty()) {
            writeResponse(response, "ID заказа не указан", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            int id = Integer.parseInt(orderId);
            Order order = orderDao.getOrderById(id);
            if (order != null) {
                OrderDto orderDto = orderConverter.convertEntityToDto(order);
                writeResponse(response, gson.toJson(orderDto), HttpServletResponse.SC_OK);
            } else {
                writeResponse(response, ORDER_NOT_FOUND, HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            writeResponse(response, "ID заказа должен быть числом", HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.error("Ошибка доступа к базе данных", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Неизвестная ошибка", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обрабатывает HTTP POST запросы для создания нового заказа. Принимает данные в формате JSON,
     * преобразует их в DTO, затем в сущность заказа и сохраняет в базе данных.
     * При успешном добавлении возвращает созданный объект заказа в формате JSON.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, который содержит ответ сервлета на запрос клиента.
     * @throws IOException если происходит ошибка ввода или вывода при обработке запроса.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            OrderDto orderDto = gson.fromJson(request.getReader(), OrderDto.class);
            if (orderDto == null) {
                writeResponse(response, "Некорректные данные заказа", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            Order order = orderConverter.convertDTOToEntity(orderDto);
            orderDao.addOrder(order);
            writeResponse(response, gson.toJson(orderDto), HttpServletResponse.SC_CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("Ошибка разбора JSON", e);
            writeResponse(response, "Некорректный JSON: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.error("Ошибка базы данных при добавлении заказа", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Неизвестная ошибка при добавлении заказа", e);
            writeResponse(response, ERROR_PROCESSING + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}