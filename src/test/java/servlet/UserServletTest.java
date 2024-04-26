package servlet;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.google.gson.Gson;
import dao.UserDao;
import jakarta.servlet.http.*;
import model.dto.UserDto;
import model.entity.User;
import org.junit.jupiter.api.*;

import java.io.*;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Тестовый класс для {@link UserServlet}, который проверяет функциональность сервлета по управлению пользователями.
 * Тесты проверяют методы получения, добавления, обновления и удаления пользователей, используя моки для изоляции от базы данных.
 */
class UserServletTest {
    private UserServlet servlet;
    private UserDao mockUserDao;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private StringWriter responseWriter;
    private Gson gson;

    /**
     * Настраивает тестовую среду перед каждым тестом. Инициализирует сервлет и моки для DAO и HTTP-компонентов,
     * включая подмену потока вывода для проверки результатов.
     * @throws Exception если возникают ошибки доступа к полям сервлета через рефлексию.
     */
    @BeforeEach
    public void setup() throws Exception {
        mockUserDao = mock(UserDao.class);
        gson = new Gson();
        servlet = new UserServlet();

        Field userDaoField = UserServlet.class.getDeclaredField("userDao");
        userDaoField.setAccessible(true);
        userDaoField.set(servlet, mockUserDao);

        Field gsonField = UserServlet.class.getDeclaredField("gson");
        gsonField.setAccessible(true);
        gsonField.set(servlet, gson);

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);

        responseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(responseWriter);
        when(mockResponse.getWriter()).thenReturn(writer);
    }

    /**
     * Тестирует метод doGet для получения данных одного пользователя по ID.
     * Проверяет правильность статуса ответа и корректность данных пользователя в формате JSON.
     * @throws Exception если возникают исключения в процессе выполнения.
     */
    @Test
    void testDoGetSingleUser() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn("1");
        User mockUser = new User(1, "testUser", "test@example.com");
        when(mockUserDao.getUserById(1)).thenReturn(mockUser);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertEquals(gson.toJson(new UserDto(1, "testUser", "test@example.com")), responseWriter.toString().trim());
    }

    /**
     * Тестирует метод doGet для получения данных всех пользователей.
     * Проверяет, что ответ содержит информацию о пользователях в формате JSON.
     * @throws Exception если возникают исключения в процессе выполнения.
     */
    @Test
    void testDoGetAllUsers() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn(null);
        List<User> users = List.of(new User(1, "testUser", "test@example.com"));
        when(mockUserDao.getAllUsers()).thenReturn(users);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertThat(responseWriter.toString(), containsString("testUser"));
    }

    /**
     * Тестирует метод doPost для добавления нового пользователя.
     * Проверяет, что пользователь добавляется в базу данных и сервлет устанавливает статус CREATED.
     * @throws Exception если возникают исключения в процессе выполнения.
     */
    @Test
    void testDoPost() throws Exception {
        UserDto newUser = new UserDto(0, "newUser", "new@example.com");
        String json = gson.toJson(newUser);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        servlet.doPost(mockRequest, mockResponse);

        verify(mockUserDao).addUser(any(User.class));
        verify(mockResponse).setStatus(HttpServletResponse.SC_CREATED);
    }

    /**
     * Тестирует метод doPut для обновления существующего пользователя.
     * Проверяет, что данные пользователя обновляются в базе данных и сервлет устанавливает статус OK.
     * @throws Exception если возникают исключения в процессе выполнения.
     */
    @Test
    void testDoPut() throws Exception {
        UserDto updatedUser = new UserDto(1, "updatedUser", "updated@example.com");
        String json = gson.toJson(updatedUser);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        servlet.doPut(mockRequest, mockResponse);

        verify(mockUserDao).updateUser(any(User.class));
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Тестирует метод doDelete для удаления пользователя по ID.
     * Проверяет, что пользователь удаляется из базы данных и сервлет устанавливает статус OK.
     * @throws Exception если возникают исключения в процессе выполнения.
     */
    @Test
    void testDoDelete() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn("1");

        servlet.doDelete(mockRequest, mockResponse);

        verify(mockUserDao).deleteUser(1);
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
    }
}