package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;
import dao.UserDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import model.dto.UserDto;
import model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static constants.ServletConstants.*;
import static util.ServletUtils.writeResponse;

/**
 * Сервлет для управления пользователями через REST API.
 * Поддерживает операции получения, добавления, обновления и удаления пользователей.
 */
@WebServlet("/users")
@AllArgsConstructor
public class UserServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);

    private UserDao userDao;
    private Gson gson;

    /**
     * Обрабатывает HTTP GET запросы для получения одного пользователя по ID или всех пользователей.
     * Возвращает данные в формате JSON.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, содержащий ответ сервлета клиенту.
     * @throws IOException при ошибках ввода/вывода.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userId = request.getParameter("id");
        try {
            if (userId != null) {
                User user = userDao.getUserById(Integer.parseInt(userId));
                if (user != null) {
                    UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getEmail());
                    writeResponse(response, gson.toJson(userDto), HttpServletResponse.SC_OK);
                } else {
                    writeResponse(response, USER_NOT_FOUND, HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                List<User> users = userDao.getAllUsers();
                List<UserDto> userDtos = users.stream()
                        .map(u -> new UserDto(u.getId(), u.getUsername(), u.getEmail()))
                        .toList();
                writeResponse(response, gson.toJson(userDtos), HttpServletResponse.SC_OK);
            }
        } catch (NumberFormatException e) {
            writeResponse(response, "Идентификатор пользователя должен быть действительным целым числом.",
                    HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            writeResponse(response, "Ошибка доступа к базе данных", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.error("Ошибка доступа к базе данных при попытке получить пользователя по идентификатору", e);
        } catch (Exception e) {
            writeResponse(response, "Внутренняя ошибка сервера", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.error("Неожиданная ошибка при попытке получить пользователя", e);
        }
    }

    /**
     * Обрабатывает HTTP POST запросы для добавления нового пользователя.
     * Принимает данные пользователя в формате JSON.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, который содержит ответ сервлета на запрос клиента.
     * @throws IOException при ошибках ввода/вывода.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UserDto userDto = gson.fromJson(request.getReader(), UserDto.class);
            User user = new User(userDto.getId(), userDto.getUserName(), userDto.getEmail());
            userDao.addUser(user);
            writeResponse(response, USER_ADDED, HttpServletResponse.SC_CREATED);
        } catch (IOException | IllegalStateException e) {
            logger.error("Ошибка обработки запроса", e);
            writeResponse(response, "Ошибка обработки запроса: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.error("Ошибка доступа к базе данных при создании пользователя", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании пользователя", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обрабатывает HTTP PUT запросы для обновления существующего пользователя.
     * Принимает обновлённые данные пользователя в формате JSON.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, который содержит ответ сервлета на запрос клиента.
     * @throws IOException при ошибках ввода/вывода.
     */
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UserDto userDto = gson.fromJson(request.getReader(), UserDto.class);
            User user = new User(userDto.getId(), userDto.getUserName(), userDto.getEmail());
            userDao.updateUser(user);
            writeResponse(response, USER_UPDATED, HttpServletResponse.SC_OK);
        } catch (IOException | IllegalStateException e) {
            logger.error("Ошибка обработки запроса", e);
            writeResponse(response, "Ошибка обработки запроса: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.error("Ошибка доступа к базе данных во время обновления пользователя", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Неожиданная ошибка во время обновления пользователя", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обрабатывает HTTP DELETE запросы для удаления пользователя по его идентификатору.
     * Удаляет пользователя из базы данных.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, который содержит ответ сервлета на запрос клиента.
     * @throws IOException при ошибках ввода/вывода.
     */
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userId = request.getParameter("id");
        try {
            int id = Integer.parseInt(userId);
            userDao.deleteUser(id);
            writeResponse(response, USER_DELETED, HttpServletResponse.SC_OK);
        } catch (NumberFormatException e) {
            logger.error("Неверный формат идентификатора пользователя", e);
            writeResponse(response, "Неверный формат идентификатора пользователя", HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.error("Ошибка доступа к базе данных при удалении пользователя", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении пользователя", e);
            writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}