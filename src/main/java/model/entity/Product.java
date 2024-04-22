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
    /**
     * Идентификатор продукта, уникальный ключ в базе данных.
     */
    private int id;

    /**
     * Название продукта, отражает сущность товара и используется в интерфейсах пользователя.
     */
    private String name;

    /**
     * Цена продукта, указывается в единицах валюты системы и используется при формировании заказов.
     */
    private double price;

    /**
     * Список заказов, в которые включен данный продукт.
     * Этот список помогает отслеживать все заказы, содержащие данный продукт.
     */
    private List<Order> orders;

    /**
     * Конструктор для создания объекта продукта с начальными параметрами.
     *
     * @param id Идентификатор продукта.
     * @param name Название продукта.
     * @param price Цена продукта.
     */
    public Product(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.orders = new ArrayList<>();  // Инициализация пустого списка заказов
    }
}
