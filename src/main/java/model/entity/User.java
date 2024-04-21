package model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс User представляет собой модель данных пользователя.
 * Он включает информацию, такую как идентификатор, имя пользователя и адрес электронной почты,
 * а также список заказов, ассоциированных с пользователем.
 */
@Data
public class User {
    private int id;
    private String username;
    private String email;
    private List<Order> orders;

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.orders = new ArrayList<>();
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.orders = new ArrayList<>();
    }
}
