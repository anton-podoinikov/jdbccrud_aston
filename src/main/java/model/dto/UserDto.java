package model.dto;

/**
 * Класс DTO для пользователя.
 * Используется для передачи данных пользователя между клиентом и сервером.
 */
public class UserDto {
    private int id;
    private String username;
    private String email;

    public UserDto() {}

    public UserDto(int id, String userName, String email) {
        this.id = id;
        this.username = userName;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
