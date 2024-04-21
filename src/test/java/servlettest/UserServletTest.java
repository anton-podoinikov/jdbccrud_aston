package servlettest;

import com.google.gson.Gson;
import dao.UserDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.UserDto;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import servlet.UserServlet;

import java.io.*;
import java.sql.SQLException;
import java.util.List;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Класс тестов для {@link UserServlet}.
 * Проверяет функциональность методов doGet, doPost, doPut и doDelete, взаимодействуя с UserDao.
 */
class UserServletTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private UserDao userDao;

    private UserServlet servlet;
    private StringWriter responseWriter;
    private final Gson gson = new Gson();

    /**
     * Подготавливает окружение перед каждым тестом. Инициализирует моки и объекты для тестирования.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        responseWriter = new StringWriter();
        servlet = new UserServlet(userDao, gson);

        try {
            when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Тестирует получение данных одного пользователя по ID.
     * Удостоверяется, что данные пользователя корректно возвращаются в формате JSON.
     */
    @Test
    void doGetSingleUser() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        User user = new User(1, "Ivan", "ivan@gmail.com");
        when(userDao.getUserById(1)).thenReturn(user);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals(gson.toJson(new UserDto(1, "Ivan", "ivan@gmail.com")),
                responseWriter.toString().trim());
    }

    /**
     * Тестирует получение списка всех пользователей.
     * Проверяет, что JSON со списком пользователей возвращается правильно.
     */
    @Test
    void doGetAllUsers() throws Exception {
        when(request.getParameter("id")).thenReturn(null);
        when(userDao.getAllUsers()).thenReturn(List.of(
                new User(1, "Ivan", "ivan@gmail.com"),
                new User(2, "Anton", "anton@gmail.com")
        ));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals(gson.toJson(List.of(
                new UserDto(1, "Ivan", "ivan@gmail.com"),
                new UserDto(2, "Anton", "anton@gmail.com")
        )), responseWriter.toString().trim());
    }

    /**
     * Тестирует создание нового пользователя через POST запрос.
     * Удостоверяется, что пользователь добавляется в базу данных и возвращается соответствующий HTTP статус.
     */
    @Test
    void doPostNewUser() throws Exception {
        String jsonInput = "{\"id\":3,\"userName\":\"Oleg\",\"email\":\"oleg@gmail.com\"}";
        User newUser = new User(3, "Oleg", "oleg@gmail.com");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonInput)));

        servlet.doPost(request, response);

        verify(userDao).addUser(refEq(newUser));
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    /**
     * Тестирует обновление данных пользователя через PUT запрос.
     * Проверяет, что данные пользователя успешно обновляются и возвращается статус HTTP OK.
     */
    @Test
    void doPutUpdateUser() throws Exception {
        String jsonInput = "{\"id\":1,\"userName\":\"UpdatedIvan\",\"email\":\"updatedivan@gmail.com\"}";
        User updatedUser = new User(1, "UpdatedIvan", "updatedivan@gmail.com");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonInput)));

        servlet.doPut(request, response);

        verify(userDao).updateUser(refEq(updatedUser));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Тестирует удаление пользователя по ID через DELETE запрос.
     * Проверяет, что пользователь удаляется из базы данных и возвращается статус HTTP OK.
     */
    @Test
    void doDeleteUser() throws Exception {
        when(request.getParameter("id")).thenReturn("1");

        servlet.doDelete(request, response);

        verify(userDao).deleteUser(1);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Тестирует случай, когда попытка удаления пользователя вызывает исключение.
     * Проверяет, что возвращается статус HTTP Internal Server Error.
     */
    @Test
    void doDeleteUserNotFound() throws Exception {
        doThrow(new SQLException()).when(userDao).deleteUser(anyInt());
        when(request.getParameter("id")).thenReturn("999");

        servlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}