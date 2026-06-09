package com.kasir.retail.model;

public class Product {
    private int id;
    private String code;
    private String name;
    private int categoryId;
    private String categoryName;
    private double price;
    private int stock;

    public Product() {}

    public Product(int id, String code, String name, int categoryId, double price, int stock) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.stock = stock;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toString() {
        return String.format("[%s] %s - Rp%,.0f (Stok: %d)", code, name, price, stock);
    }
}
