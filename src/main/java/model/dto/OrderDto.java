package model.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс DTO для заказа.
 * Используется для передачи данных о заказах между клиентом и сервером, включая подробную информацию о продуктах.
 */
public class OrderDto {
    private int id;
    private int userId;
    private List<ProductInfo> products;
    private List<Integer> productIds;

    // Класс для хранения информации о продукте
    public static class ProductInfo {
        private int productId;
        private String name;
        private double price;

        public ProductInfo(int productId, String name, double price) {
            this.productId = productId;
            this.name = name;
            this.price = price;
        }

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }

    public List<Integer> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Integer> productIds) {
        this.productIds = productIds;
    }

    public OrderDto() {
        this.products = new ArrayList<>();
    }

    public OrderDto(int id, int userId, List<ProductInfo> products) {
        this.id = id;
        this.userId = userId;
        this.products = products == null ? new ArrayList<>() : new ArrayList<>(products);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<ProductInfo> getProducts() {
        return products;
    }

    public void setProducts(List<ProductInfo> products) {
        this.products = products == null ? new ArrayList<>() : new ArrayList<>(products);
    }
}
