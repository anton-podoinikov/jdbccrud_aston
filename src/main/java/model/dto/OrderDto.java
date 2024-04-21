package model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Класс DTO для заказа.
 * Используется для передачи данных о заказах между клиентом и сервером,
 * включая подробную информацию о продуктах и пользователях.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private int id;
    private UserDto user; // Использование объекта UserDto для хранения данных пользователя.
    private List<ProductDto> products; // Используем List<ProductDto> для продуктов.
}
