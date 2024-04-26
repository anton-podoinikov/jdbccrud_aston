package model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс Order представляет сущность заказа в системе.
 * Этот класс содержит информацию о заказе, включая его идентификатор, пользователя, сделавшего заказ,
 * и список продуктов, включенных в заказ.
 * Использование аннотации @Data из библиотеки Lombok позволяет автоматически генерировать
 * геттеры, сеттеры и методы hashCode(), equals() и toString() для всех полей класса,
 * уменьшая объем кода и упрощая поддержку.
 */
@Data
public class Order {

    private int id;

    private User user;

    private List<Product> products;

    public Order() {
        this.products = new ArrayList<>();
    }

    public Order(int id) {
        this.id = id;
        this.products = new ArrayList<>();
    }
}
