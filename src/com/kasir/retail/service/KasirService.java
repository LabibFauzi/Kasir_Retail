package com.kasir.retail.service;

import com.kasir.retail.dao.*;
import com.kasir.retail.model.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class KasirService {
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final TransactionDAO transactionDAO;
    private final List<CartItem> cart;
    private int invoiceCounter;

    public KasirService() {
        this.productDAO = new ProductDAO();
        this.categoryDAO = new CategoryDAO();
        this.transactionDAO = new TransactionDAO();
        this.cart = new ArrayList<>();
        this.invoiceCounter = loadInvoiceCounter();
    }

    private int loadInvoiceCounter() {
        try {
            return transactionDAO.getNextInvoiceCounter();
        } catch (SQLException e) {
            return 1;
        }
    }

    // Category operations
    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.getAll();
    }

    public void addCategory(String name) throws SQLException {
        categoryDAO.insert(name);
    }

    public void updateCategory(int id, String name) throws SQLException {
        categoryDAO.update(id, name);
    }

    public void deleteCategory(int id) throws SQLException {
        categoryDAO.delete(id);
    }

    // Product operations
    public List<Product> getAllProducts() throws SQLException {
        return productDAO.getAll();
    }

    public List<Product> searchProducts(String keyword) throws SQLException {
        return productDAO.search(keyword);
    }

    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        return productDAO.getByCategory(categoryId);
    }

    public Product getProductById(int id) throws SQLException {
        return productDAO.getById(id);
    }

    public Product getProductByCode(String code) throws SQLException {
        return productDAO.getByCode(code);
    }

    public void addProduct(String code, String name, int categoryId, double price, int stock) throws SQLException {
        Product p = new Product(0, code, name, categoryId, price, stock);
        productDAO.insert(p);
    }

    public String generateNextProductCode() throws SQLException {
        String maxCode = productDAO.getMaxCode();
        if (maxCode == null || maxCode.isEmpty()) return "P001";
        String numPart = maxCode.replaceAll("[^0-9]", "");
        if (numPart.isEmpty()) return maxCode + "1";
        int num = Integer.parseInt(numPart) + 1;
        String fmt = "%0" + numPart.length() + "d";
        String prefix = maxCode.substring(0, maxCode.length() - numPart.length());
        return prefix + String.format(fmt, num);
    }

    public void updateProduct(int id, String code, String name, int categoryId, double price, int stock) throws SQLException {
        Product p = new Product(id, code, name, categoryId, price, stock);
        productDAO.update(p);
    }

    public void deleteProduct(int id) throws SQLException {
        productDAO.delete(id);
    }

    // Cart operations
    public void updateCartQuantity(int index, int newQty) throws Exception {
        if (index < 0 || index >= cart.size()) return;
        CartItem item = cart.get(index);
        if (newQty <= 0) {
            cart.remove(index);
            return;
        }
        if (item.getProduct().getStock() < newQty) {
            throw new Exception("Stok tidak mencukupi! Stok tersedia: " + item.getProduct().getStock());
        }
        item.setQuantity(newQty);
    }

    public void addToCart(String code, int quantity) throws Exception {
        Product product = productDAO.getByCode(code);
        if (product == null) {
            throw new Exception("Produk dengan kode " + code + " tidak ditemukan!");
        }
        if (product.getStock() < quantity) {
            throw new Exception("Stok tidak mencukupi! Stok tersedia: " + product.getStock());
        }

        for (CartItem item : cart) {
            if (item.getProduct().getId() == product.getId()) {
                int newQty = item.getQuantity() + quantity;
                if (product.getStock() < newQty) {
                    throw new Exception("Stok tidak mencukupi! Stok tersedia: " + product.getStock());
                }
                item.setQuantity(newQty);
                return;
            }
        }
        cart.add(new CartItem(product, quantity));
    }

    public void removeFromCart(int index) {
        if (index >= 0 && index < cart.size()) {
            cart.remove(index);
        }
    }

    public void clearCart() {
        cart.clear();
    }

    public List<CartItem> getCart() {
        return cart;
    }

    public double getCartTotal() {
        return cart.stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public boolean isCartEmpty() {
        return cart.isEmpty();
    }

    // Transaction operations
    public Transaction checkout(double payment) throws Exception {
        if (cart.isEmpty()) {
            throw new Exception("Keranjang belanja kosong!");
        }

        double total = getCartTotal();
        if (payment < total) {
            throw new Exception(String.format("Pembayaran kurang! Total: Rp%,.0f, Bayar: Rp%,.0f", total, payment));
        }

        String invoice = generateInvoice();
        Transaction transaction = new Transaction();
        transaction.setInvoice(invoice);
        transaction.setDate(LocalDateTime.now());
        transaction.setTotal(total);
        transaction.setPayment(payment);
        transaction.setChange(payment - total);

        for (CartItem ci : cart) {
            TransactionItem item = new TransactionItem();
            item.setProductId(ci.getProduct().getId());
            item.setProductName(ci.getProduct().getName());
            item.setPrice(ci.getProduct().getPrice());
            item.setQuantity(ci.getQuantity());
            item.setSubtotal(ci.getSubtotal());
            transaction.getItems().add(item);

            productDAO.updateStock(ci.getProduct().getId(), ci.getQuantity());
        }

        transactionDAO.insert(transaction);
        cart.clear();
        invoiceCounter++;

        return transaction;
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        return transactionDAO.getAll();
    }

    public Transaction getTransactionDetail(int id) throws SQLException {
        return transactionDAO.getById(id);
    }

    public double getTotalSalesToday() throws SQLException {
        return transactionDAO.getTotalSalesToday();
    }

    private String generateInvoice() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String seq = String.format("%04d", invoiceCounter);
        return "INV-" + datePart + "-" + seq;
    }
}
