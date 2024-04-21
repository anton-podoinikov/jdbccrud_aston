package exeption;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

import static constants.ServletConstants.*;

public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    public static void handleException(Exception e, HttpServletResponse response, String jsonResponse) {
        try {
            if (e instanceof NumberFormatException) {
                logger.error("Ошибка: исключение формата числа.", e);
                writeResponse(response, "Некорректный формат числа: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            } else if (e instanceof SQLException) {
                logger.error("Ошибка доступа к базе данных", e);
                writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else if (e instanceof IOException) {
                logger.error("Ошибка ввода/вывода", e);
                writeResponse(response, "Ошибка ввода/вывода: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            } else {
                logger.error("произошла непредвиденная ошибка", e);
                writeResponse(response, ERROR_SERVER, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (IOException ioException) {
            logger.error("Не удалось написать ответ", ioException);
        }
    }

    private static void writeResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write(message);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }
}