package util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Утилитный класс, предоставляющий статические методы для помощи в работе с сервлетами.
 * Этот класс содержит методы для стандартной обработки HTTP ответов.
 */
public class ServletUtils {

    /**
     * Приватный конструктор для предотвращения создания экземпляра утилитного класса.
     */
    private ServletUtils() {
    }

    /**
     * Отправляет ответ клиенту с заданным содержимым и HTTP статусом.
     * Метод устанавливает тип содержимого ответа как JSON и кодировку UTF-8,
     * что обеспечивает корректное отображение содержимого и его правильное интерпретирование клиентами.
     *
     * @param response Объект HttpServletResponse, который используется для отправки данных клиенту.
     * @param message Сообщение или данные в формате JSON, которые будут отправлены клиенту.
     * @param status HTTP статус код ответа.
     * @throws IOException Если произошла ошибка ввода-вывода при попытке получить Writer от ответа.
     */
    public static void writeResponse(HttpServletResponse response,
                                     String message,
                                     int status) throws IOException {
        response.setContentType("application/json"); // Установка типа содержимого ответа
        response.setCharacterEncoding("UTF-8"); // Установка кодировки символов
        response.setStatus(status); // Установка HTTP статуса ответа
        PrintWriter out = response.getWriter(); // Получение writer'а для записи ответа
        out.println(message); // Запись сообщения в ответ
        out.flush(); // Очистка потока вывода, гарантирующая отправку данных
    }
}
