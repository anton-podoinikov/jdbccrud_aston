package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dao.ProductDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import model.dto.ProductDto;
import model.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static constants.ServletConstants.*;
import static util.ServletUtils.writeResponse;

/**
 * Сервлет для управления продуктами через REST API.
 * Обрабатывает запросы на получение, добавление, обновление и удаление продуктов.
 */
@WebServlet("/products")
@AllArgsConstructor
public class ProductServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProductServlet.class);

    private ProductDao productDao;
    private Gson gson;

    /**
     * Обрабатывает HTTP GET запросы для получения продукта или списка продуктов.
     * Если указан параметр id, возвращает конкретный продукт, иначе возвращает список всех продуктов.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, содержащий ответ сервлета клиенту.
     * @throws IOException при ошибках ввода/вывода.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String productId = request.getParameter("id");
        try {
            if (productId != null) {
                int id = Integer.parseInt(productId);
                Product product = productDao.getProductById(id);
                if (product != null) {
                    ProductDto productDto = new ProductDto(product.getId(), product.getName(), product.getPrice());
                    writeResponse(response, gson.toJson(productDto), HttpServletResponse.SC_OK);
                } else {
                    writeResponse(response, PRODUCT_NOT_FOUND, HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                List<Product> products = productDao.getAllProducts();
                List<ProductDto> productDtos = products.stream()
                        .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice()))
                        .toList();
                writeResponse(response, gson.toJson(productDtos), HttpServletResponse.SC_OK);
            }
        } catch (NumberFormatException e) {
            logger.error("Ошибка преобразования ID продукта", e);
            writeResponse(response, "ID продукта должен быть числом", HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.error("Ошибка доступа к базе данных", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Неизвестная ошибка", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обрабатывает HTTP POST запросы для добавления нового продукта.
     * Принимает данные продукта в формате JSON и добавляет продукт в базу данных.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, который содержит ответ сервлета на запрос клиента.
     * @throws IOException при ошибках ввода/вывода.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ProductDto productDto = gson.fromJson(request.getReader(), ProductDto.class);
            if (productDto == null) {
                writeResponse(response, "Некорректные данные продукта", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            Product product = new Product(productDto.getId(), productDto.getName(), productDto.getPrice());
            productDao.addProduct(product);
            writeResponse(response, PRODUCT_ADDED, HttpServletResponse.SC_CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("Ошибка разбора JSON", e);
            writeResponse(response, "Некорректный JSON: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Неизвестная ошибка при добавлении продукта", e);
            writeResponse(response, ERROR_PROCESSING + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обрабатывает HTTP PUT запросы для обновления существующего продукта.
     * Принимает обновлённые данные продукта в формате JSON и обновляет данные продукта в базе данных.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, который содержит ответ сервлета на запрос клиента.
     * @throws IOException при ошибках ввода/вывода.
     */
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ProductDto productDto = gson.fromJson(request.getReader(), ProductDto.class);
            Product product = new Product(productDto.getId(), productDto.getName(), productDto.getPrice());
            productDao.updateProduct(product);
            writeResponse(response, PRODUCT_UPDATED, HttpServletResponse.SC_OK);
        } catch (JsonSyntaxException e) {
            logger.error("Ошибка разбора JSON", e);
            writeResponse(response, "Некорректный JSON: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.error("Ошибка базы данных при обновлении продукта", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Неизвестная ошибка при обновлении продукта", e);
            writeResponse(response, ERROR_PROCESSING + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Обрабатывает HTTP DELETE запросы для удаления продукта по идентификатору.
     * Удаляет продукт из базы данных.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, который содержит ответ сервлета на запрос клиента.
     * @throws IOException при ошибках ввода/вывода.
     */
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String productId = request.getParameter("id");
        try {
            int id = Integer.parseInt(productId);
            productDao.deleteProduct(id);
            writeResponse(response, PRODUCT_DELETED, HttpServletResponse.SC_OK);
        } catch (NumberFormatException e) {
            logger.error("Ошибка преобразования ID продукта", e);
            writeResponse(response, "ID продукта должен быть числом", HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.error("Ошибка базы данных при удалении продукта", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Неизвестная ошибка при удалении продукта", e);
            writeResponse(response, ERROR_PROCESSING + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
