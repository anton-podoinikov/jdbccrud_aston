package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import dao.ProductDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.ProductDto;
import model.entity.Product;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static util.ServletUtils.writeResponse;

/**
 * Сервлет для управления продуктами через REST API.
 * Обрабатывает запросы на получение, добавление, обновление и удаление продуктов.
 */
@WebServlet("/products")
public class ProductServlet extends HttpServlet {
    private final ProductDao productDao = new ProductDao();
    private final Gson gson = new Gson();

    /**
     * Обрабатывает HTTP GET запросы для получения продукта или списка продуктов.
     * Если указан параметр id, возвращает конкретный продукт, иначе возвращает список всех продуктов.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, содержащий ответ сервлета клиенту.
     * @throws IOException при ошибках ввода/вывода.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String productId = request.getParameter("id");
        try {
            if(productId != null) {
                Product product = productDao.getProductById(Integer.parseInt(productId));
                if (product != null) {
                    ProductDto productDto = new ProductDto(product.getId(), product.getName(), product.getPrice());
                    writeResponse(response, gson.toJson(productDto), HttpServletResponse.SC_OK);
                } else {
                    writeResponse(response, "Продукт не найден", HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                List<Product> products = productDao.getAllProducts();
                List<ProductDto> productDtos = products.stream()
                        .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice()))
                        .collect(Collectors.toList());
                writeResponse(response, gson.toJson(productDtos), HttpServletResponse.SC_OK);
            }
        } catch (SQLException e) {
            writeResponse(response, "Внутренняя ошибка сервера", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ProductDto productDto = gson.fromJson(request.getReader(), ProductDto.class);
            Product product = new Product(productDto.getId(), productDto.getName(), productDto.getPrice());
            productDao.addProduct(product);
            writeResponse(response, "Продукт успешно добавлен", HttpServletResponse.SC_CREATED);
        } catch (JsonIOException e) {
            writeResponse(response, "Json-данные некорректны", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            writeResponse(response, "Json-данные некорректны", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        } catch (IOException e) {
            writeResponse(response, "Внутренняя ошибка сервера", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
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
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ProductDto productDto = gson.fromJson(request.getReader(), ProductDto.class);
            Product product = new Product(productDto.getId(), productDto.getName(), productDto.getPrice());
            productDao.updateProduct(product);
            writeResponse(response, "Продукт успешно обновлен", HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            writeResponse(response, "Внутренняя ошибка сервера", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
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
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String productId = request.getParameter("id");
        try {
            productDao.deleteProduct(Integer.parseInt(productId));
            writeResponse(response, "Продукт успешно удален", HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            writeResponse(response, "Внутренняя ошибка сервера", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
