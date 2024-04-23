package servlet;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.google.gson.Gson;
import dao.ProductDao;
import jakarta.servlet.http.*;
import model.dto.ProductDto;
import model.entity.Product;
import org.junit.jupiter.api.*;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

/**
 * Класс тестов для {@link ProductServlet}. Проверяет функциональность получения, добавления, обновления и удаления продуктов.
 * Использует моки для DAO и HTTP сервлет запросов и ответов, чтобы изолировать тестирование от базы данных и сетевых вызовов.
 */
public class ProductServletTest {
    private ProductServlet servlet;
    private ProductDao mockProductDao;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private StringWriter responseWriter;
    private Gson gson;

    /**
     * Подготавливает необходимую среду для тестирования, включая моки для зависимостей и инъекцию их через рефлексию.
     * @throws Exception Если произойдет ошибка доступа к полям сервлета.
     */
    @BeforeEach
    public void setup() throws Exception {

        mockProductDao = mock(ProductDao.class);
        gson = new Gson();
        servlet = new ProductServlet();

        Field daoField = ProductServlet.class.getDeclaredField("productDao");
        daoField.setAccessible(true);
        daoField.set(servlet, mockProductDao);

        Field gsonField = ProductServlet.class.getDeclaredField("gson");
        gsonField.setAccessible(true);
        gsonField.set(servlet, gson);

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(responseWriter);
        when(mockResponse.getWriter()).thenReturn(writer);
    }

    /**
     * Тестирует получение данных о конкретном продукте по ID.
     * Проверяет, что сервлет устанавливает корректный статус ответа и возвращает данные продукта в формате JSON.
     * @throws Exception если возникнут ошибки ввода/вывода.
     */
    @Test
    public void testDoGetSingleProduct() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn("1");
        Product mockProduct = new Product(1, "Test Product", 100.0);
        when(mockProductDao.getProductById(1)).thenReturn(mockProduct);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertEquals(gson.toJson(new ProductDto(1, "Test Product", 100.0)), responseWriter.toString().trim());
    }

    /**
     * Тестирует получение данных о всех продуктах.
     * Проверяет, что сервлет корректно обрабатывает ответ, содержащий информацию о продуктах.
     * @throws Exception если возникнут ошибки ввода/вывода.
     */
    @Test
    public void testDoGetAllProducts() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn(null);
        List<Product> products = List.of(new Product(1, "Test Product", 100.0));
        when(mockProductDao.getAllProducts()).thenReturn(products);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertThat(responseWriter.toString().trim(), containsString("Test Product"));
    }

    /**
     * Тестирует добавление нового продукта через POST запрос.
     * Проверяет, что сервлет корректно обрабатывает входные данные и устанавливает статус CREATED после добавления продукта.
     * @throws Exception если возникнут ошибки ввода/вывода.
     */
    @Test
    public void testDoPost() throws Exception {
        ProductDto newProduct = new ProductDto(0, "New Product", 150.0);
        String jsonInput = gson.toJson(newProduct);
        BufferedReader reader = new BufferedReader(new StringReader(jsonInput));
        when(mockRequest.getReader()).thenReturn(reader);

        servlet.doPost(mockRequest, mockResponse);

        verify(mockProductDao).addProduct(any(Product.class));
        verify(mockResponse).setStatus(HttpServletResponse.SC_CREATED);
    }

    /**
     * Тестирует обновление данных продукта через PUT запрос.
     * Проверяет, что сервлет корректно обрабатывает входные данные и устанавливает статус OK после обновления продукта.
     * @throws Exception если возникнут ошибки ввода/вывода.
     */
    @Test
    public void testDoPut() throws Exception {
        ProductDto productDto = new ProductDto(1, "Updated Product", 150.0);
        String jsonInput = gson.toJson(productDto);
        BufferedReader reader = new BufferedReader(new StringReader(jsonInput));
        when(mockRequest.getReader()).thenReturn(reader);

        servlet.doPut(mockRequest, mockResponse);

        verify(mockProductDao).updateProduct(any(Product.class));
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Тестирует удаление продукта по ID через DELETE запрос.
     * Проверяет, что сервлет корректно обрабатывает удаление и устанавливает соответствующий статус ответа.
     * @throws Exception если возникнут ошибки ввода/вывода.
     */
    @Test
    public void testDoDelete() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn("1");

        servlet.doDelete(mockRequest, mockResponse);

        verify(mockProductDao).deleteProduct(1);
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Тестирует обработку исключения SQLException при попытке удаления продукта.
     * Проверяет, что сервлет корректно обрабатывает исключение и устанавливает статус ответа INTERNAL_SERVER_ERROR.
     * @throws IOException если возникнут ошибки ввода/вывода.
     * @throws SQLException если возникнет ошибка доступа к базе данных.
     */
    @Test
    public void testDoDeleteWithSqlException() throws IOException, SQLException {
        when(mockRequest.getParameter("id")).thenReturn("1");
        doThrow(SQLException.class).when(mockProductDao).deleteProduct(1);

        servlet.doDelete(mockRequest, mockResponse);

        verify(mockResponse).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}