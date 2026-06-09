package com.kasir.retail.dao;

import com.kasir.retail.database.DatabaseManager;
import com.kasir.retail.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAll() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "JOIN categories c ON p.category_id = c.id ORDER BY p.id";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        }
        return list;
    }

    public List<Product> search(String keyword) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.code LIKE ? OR p.name LIKE ? ORDER BY p.id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        }
        return list;
    }

    public Product getById(int id) throws SQLException {
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProduct(rs);
            }
        }
        return null;
    }

    public Product getByCode(String code) throws SQLException {
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "JOIN categories c ON p.category_id = c.id WHERE p.code = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProduct(rs);
            }
        }
        return null;
    }

    public void insert(Product product) throws SQLException {
        String sql = "INSERT INTO products (code, name, category_id, price, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getCode());
            ps.setString(2, product.getName());
            ps.setInt(3, product.getCategoryId());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getStock());
            ps.executeUpdate();
        }
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET code = ?, name = ?, category_id = ?, price = ?, stock = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getCode());
            ps.setString(2, product.getName());
            ps.setInt(3, product.getCategoryId());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getStock());
            ps.setInt(6, product.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void updateStock(int id, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock = stock - ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCode(rs.getString("code"));
        p.setName(rs.getString("name"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setPrice(rs.getDouble("price"));
        p.setStock(rs.getInt("stock"));
        return p;
    }
}
