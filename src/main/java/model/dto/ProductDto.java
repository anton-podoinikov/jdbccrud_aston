package model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Класс Data Transfer Object (DTO) для продукта.
 * Используется для передачи данных о продуктах между клиентом и сервером.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private int id;
    private String name;
    private double price;
}
