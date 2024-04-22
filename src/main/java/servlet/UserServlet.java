package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import dao.UserDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.UserDto;
import model.entity.User;

import static util.ServletUtils.writeResponse;

/**
 * Сервлет для управления пользователями через REST API.
 * Поддерживает операции получения, добавления, обновления и удаления пользователей.
 */
@WebServlet("/users")
public class UserServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();
    private final Gson gson = new Gson();

    /**
     * Обрабатывает HTTP GET запросы для получения одного пользователя по ID или всех пользователей.
     * Возвращает данные в формате JSON.
     *
     * @param request  Объект HttpServletRequest, содержащий запрос от клиента.
     * @param response Объект HttpServletResponse, содержащий ответ сервлета клиенту.
     * @throws IOException при ошибках ввода/вывода.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userId = request.getParameter("id");
        try {
            if (userId != null) {
                User user = userDao.getUserById(Integer.parseInt(userId));
                if (user != null) {
                    UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getEmail());
                    writeResponse(response, gson.toJson(userDto), HttpServletResponse.SC_OK);
                } else {
                    writeResponse(response, "Пользователь не найден", HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                List<User> users = userDao.getAllUsers();
                List<UserDto> userDtos = users.stream()
                        .map(u -> new UserDto(u.getId(), u.getUsername(), u.getEmail()))
                        .collect(Collectors.toList());
                writeResponse(response, gson.toJson(userDtos), HttpServletResponse.SC_OK);
            }
        } catch (SQLException e) {
            writeResponse(response, "Внутренняя ошибка сервера", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UserDto userDto = gson.fromJson(request.getReader(), UserDto.class);
            User user = new User(userDto.getId(), userDto.getUsername(), userDto.getEmail());
            userDao.addUser(user);
            writeResponse(response, "Пользователь успешно добавлен", HttpServletResponse.SC_CREATED);
        } catch (SQLException e) {
            writeResponse(response, "Внутренняя ошибка сервера", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
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
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            UserDto userDto = gson.fromJson(request.getReader(), UserDto.class);
            User user = new User(userDto.getId(), userDto.getUsername(), userDto.getEmail());
            userDao.updateUser(user);
            writeResponse(response, "Пользователь успешно обновлен", HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            writeResponse(response, "Внутренняя ошибка сервера", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
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
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userId = request.getParameter("id");
        try {
            userDao.deleteUser(Integer.parseInt(userId));
            writeResponse(response, "Пользователь успешно удален", HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            writeResponse(response, "Внутренняя ошибка сервера", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
