package com.kasir.retail.model;

public class TransactionItem {
    private int id;
    private int transactionId;
    private int productId;
    private String productName;
    private double price;
    private int quantity;
    private double subtotal;

    public TransactionItem() {}

    public TransactionItem(int id, int transactionId, int productId, String productName,
                          double price, int quantity, double subtotal) {
        this.id = id;
        this.transactionId = transactionId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    @Override
    public String toString() {
        return String.format("%s x%d = Rp%,.0f", productName, quantity, subtotal);
    }
}
