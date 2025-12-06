package com.financeapi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DbUtil {

    // Change these if your DB user/password are different
    private static final String URL = "jdbc:postgresql://localhost:5432/finance_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "12345";

    public static Connection getConnection() throws SQLException {
        // With modern JDBC and the driver JAR on the classpath, this is enough.[web:41][web:64][web:78]
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Simple test method (optional)
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ Connected to PostgreSQL finance_db!");
            } else {
                System.out.println("❌ Connection is null.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
