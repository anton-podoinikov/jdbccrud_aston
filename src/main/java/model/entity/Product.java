package model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс Product представляет сущность продукта в системе.
 * Он содержит основную информацию о продукте, такую как идентификатор, название и цену,
 * а также список заказов, к которым принадлежит продукт.
 *
 * Аннотация @Data из библиотеки Lombok генерирует стандартные методы для доступа и обработки
 * полей класса, уменьшая количество шаблонного кода и упрощая разработку.
 */
@Data
public class Product {

    private int id;

    private String name;

    private double price;

    private List<Order> orders;

    public Product(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.orders = new ArrayList<>();
    }
}
