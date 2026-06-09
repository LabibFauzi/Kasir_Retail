package com.kasir.retail.database;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:database/kasir_retail.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver tidak ditemukan: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "code TEXT NOT NULL UNIQUE, " +
                    "name TEXT NOT NULL, " +
                    "category_id INTEGER NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "stock INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY (category_id) REFERENCES categories(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "invoice TEXT NOT NULL UNIQUE, " +
                    "date TEXT NOT NULL, " +
                    "total REAL NOT NULL, " +
                    "payment REAL NOT NULL, " +
                    "change_amount REAL NOT NULL" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS transaction_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "transaction_id INTEGER NOT NULL, " +
                    "product_id INTEGER NOT NULL, " +
                    "product_name TEXT NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "subtotal REAL NOT NULL, " +
                    "FOREIGN KEY (transaction_id) REFERENCES transactions(id), " +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")");

            seedData(conn);

        } catch (SQLException e) {
            System.err.println("Gagal inisialisasi database: " + e.getMessage());
        }
    }

    private static void seedData(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
        rs.next();
        if (rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO categories (name) VALUES ('Makanan')");
            stmt.execute("INSERT INTO categories (name) VALUES ('Minuman')");
            stmt.execute("INSERT INTO categories (name) VALUES ('Snack')");
            stmt.execute("INSERT INTO categories (name) VALUES ('Alat Tulis')");
            stmt.execute("INSERT INTO categories (name) VALUES ('Lainnya')");

            stmt.execute("INSERT INTO products (code, name, category_id, price, stock) " +
                    "VALUES ('BRG001', 'Nasi Goreng', 1, 15000, 50)");
            stmt.execute("INSERT INTO products (code, name, category_id, price, stock) " +
                    "VALUES ('BRG002', 'Mie Ayam', 1, 12000, 40)");
            stmt.execute("INSERT INTO products (code, name, category_id, price, stock) " +
                    "VALUES ('BRG003', 'Air Mineral', 2, 5000, 100)");
            stmt.execute("INSERT INTO products (code, name, category_id, price, stock) " +
                    "VALUES ('BRG004', 'Es Teh Manis', 2, 4000, 80)");
            stmt.execute("INSERT INTO products (code, name, category_id, price, stock) " +
                    "VALUES ('BRG005', 'Keripik Kentang', 3, 8000, 60)");
            stmt.execute("INSERT INTO products (code, name, category_id, price, stock) " +
                    "VALUES ('BRG006', 'Buku Tulis', 4, 5000, 90)");
            stmt.execute("INSERT INTO products (code, name, category_id, price, stock) " +
                    "VALUES ('BRG007', 'Pulpen', 4, 3000, 120)");
        }
    }
}
