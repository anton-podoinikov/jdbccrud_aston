package servlettest;

import com.google.gson.Gson;
import dao.ProductDao;
import model.dto.ProductDto;
import model.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.MockitoAnnotations;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlet.ProductServlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Класс тестов для {@link ProductServlet}.
 * Проверяет корректность методов doGet, doPost, doPut и doDelete для различных сценариев использования.
 */
class ProductServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ProductDao productDao;

    private ProductServlet servlet;
    private final Gson gson = new Gson();
    private StringWriter stringWriter;

    /**
     * Подготовка тестовой среды перед каждым тестом.
     * Инициализирует моки и сервлет с зависимостями.
     */
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
        servlet = new ProductServlet(productDao, gson);
    }

    /**
     * Тестирует метод doGet для получения одного продукта по ID.
     * Проверяет, что корректный JSON объект возвращается и статус ответа установлен в SC_OK.
     */
    @Test
    void doGetSingleProduct() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        Product product = new Product(1, "Молоко", 1.99);
        when(productDao.getProductById(1)).thenReturn(product);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals(gson.toJson(new ProductDto(1, "Молоко", 1.99)),
                stringWriter.toString().trim());
    }

    /**
     * Тестирует метод doGet для получения списка всех продуктов.
     * Проверяет, что корректный JSON массив возвращается и статус ответа установлен в SC_OK.
     */
    @Test
    void doGetAllProducts() throws Exception {
        when(request.getParameter("id")).thenReturn(null);
        List<Product> products = Arrays.asList(
                new Product(1, "Молоко", 1.99),
                new Product(2, "Хлеб", 0.99)
        );
        when(productDao.getAllProducts()).thenReturn(products);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(stringWriter.toString());
    }

    /**
     * Тестирует метод doPost для добавления нового продукта.
     * Проверяет, что продукт добавляется и статус ответа установлен в SC_CREATED.
     */
    @Test
    void doPost() throws Exception {
        when(request.getReader()).thenReturn(new java.io.BufferedReader
                (new java.io.StringReader("{\"id\":3,\"name\":\"Яйца\",\"price\":2.99}")));
        Product product = new Product(3, "Яйца", 2.99);

        servlet.doPost(request, response);

        verify(productDao).addProduct(product);
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    /**
     * Тестирует метод doPut для обновления существующего продукта.
     * Проверяет, что продукт обновляется и статус ответа установлен в SC_OK.
     */
    @Test
    void doPut() throws Exception {
        when(request.getReader()).thenReturn(new java.io.BufferedReader
                (new java.io.StringReader("{\"id\":1,\"name\":\"Milk\",\"price\":2.50}")));
        Product product = new Product(1, "Milk", 2.50);

        servlet.doPut(request, response);

        verify(productDao).updateProduct(product);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Тестирует метод doDelete для удаления продукта по ID.
     * Проверяет, что продукт удаляется и статус ответа установлен в SC_OK.
     */
    @Test
    void doDelete() throws Exception {
        when(request.getParameter("id")).thenReturn("1");

        servlet.doDelete(request, response);

        verify(productDao).deleteProduct(1);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }
}