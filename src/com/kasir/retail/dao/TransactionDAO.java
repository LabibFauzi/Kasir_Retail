package com.kasir.retail.dao;

import com.kasir.retail.database.DatabaseManager;
import com.kasir.retail.model.Transaction;
import com.kasir.retail.model.TransactionItem;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void insert(Transaction transaction) throws SQLException {
        String sqlTrans = "INSERT INTO transactions (invoice, date, total, payment, change_amount) VALUES (?, ?, ?, ?, ?)";
        String sqlItem = "INSERT INTO transaction_items (transaction_id, product_id, product_name, price, quantity, subtotal) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlTrans)) {
                ps.setString(1, transaction.getInvoice());
                ps.setString(2, transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                ps.setDouble(3, transaction.getTotal());
                ps.setDouble(4, transaction.getPayment());
                ps.setDouble(5, transaction.getChange());
                ps.executeUpdate();

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        int transId = rs.getInt(1);
                        try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                            for (TransactionItem item : transaction.getItems()) {
                                psItem.setInt(1, transId);
                                psItem.setInt(2, item.getProductId());
                                psItem.setString(3, item.getProductName());
                                psItem.setDouble(4, item.getPrice());
                                psItem.setInt(5, item.getQuantity());
                                psItem.setDouble(6, item.getSubtotal());
                                psItem.addBatch();
                            }
                            psItem.executeBatch();
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Transaction> getAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setInvoice(rs.getString("invoice"));
                t.setDate(LocalDateTime.parse(rs.getString("date"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                t.setTotal(rs.getDouble("total"));
                t.setPayment(rs.getDouble("payment"));
                t.setChange(rs.getDouble("change_amount"));
                list.add(t);
            }
        }
        return list;
    }

    public Transaction getById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        String sqlItem = "SELECT * FROM transaction_items WHERE transaction_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Transaction t = new Transaction();
                    t.setId(rs.getInt("id"));
                    t.setInvoice(rs.getString("invoice"));
                    t.setDate(LocalDateTime.parse(rs.getString("date"),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    t.setTotal(rs.getDouble("total"));
                    t.setPayment(rs.getDouble("payment"));
                    t.setChange(rs.getDouble("change_amount"));

                    try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                        psItem.setInt(1, id);
                        try (ResultSet rsItem = psItem.executeQuery()) {
                            while (rsItem.next()) {
                                TransactionItem item = new TransactionItem();
                                item.setId(rsItem.getInt("id"));
                                item.setTransactionId(rsItem.getInt("transaction_id"));
                                item.setProductId(rsItem.getInt("product_id"));
                                item.setProductName(rsItem.getString("product_name"));
                                item.setPrice(rsItem.getDouble("price"));
                                item.setQuantity(rsItem.getInt("quantity"));
                                item.setSubtotal(rsItem.getDouble("subtotal"));
                                t.getItems().add(item);
                            }
                        }
                    }
                    return t;
                }
            }
        }
        return null;
    }

    public int getNextInvoiceCounter() throws SQLException {
        String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        String prefix = "INV-" + today + "-";
        String sql = "SELECT invoice FROM transactions WHERE invoice LIKE ? ORDER BY invoice DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getString("invoice");
                    int lastNum = Integer.parseInt(last.substring(last.length() - 4));
                    return lastNum + 1;
                }
            }
        }
        return 1;
    }

    public double getTotalSalesToday() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM transactions WHERE date(date) = date('now', 'localtime')";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0;
    }
}
