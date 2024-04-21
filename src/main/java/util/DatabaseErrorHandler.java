package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилитный класс для обработки исключений базы данных.
 * Цель класса - централизованно обрабатывать и логировать исключения, связанные с базой данных.
 */
public class DatabaseErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseErrorHandler.class);

    private DatabaseErrorHandler() {
    }

    /**
     * Обрабатывает исключения, логирует их и выбрасывает новое исключение.
     * Этот метод улучшает видимость ошибок, добавляя к ним кастомное сообщение и логируя
     * детали перед тем, как пробросить RuntimeException для дальнейшей обработки.
     *
     * @param e             Исключение, которое нужно обработать.
     * @param customMessage Пользовательское сообщение, которое добавляется к логу ошибки.
     * @throws RuntimeException С новым сообщением и первоначальным исключением в качестве причины.
     */
    public static void handleException(Exception e, String customMessage) {
        logger.error("{}: {}", customMessage, e.getMessage(), e);
        throw new RuntimeException(customMessage, e);
    }
}
