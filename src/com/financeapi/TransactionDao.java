package com.financeapi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;


public class TransactionDao {

    private static final String SELECT_ALL_SQL =
            "SELECT id, type, amount, category, description, date FROM transactions";

    private static final String INSERT_SQL =
            "INSERT INTO transactions (type, amount, category, description, date) " +
            "VALUES (?, ?, ?, ?, ?) RETURNING id";

    public List<Transaction> getAll() {
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setType(rs.getString("type"));
                t.setAmount(rs.getDouble("amount"));
                t.setCategory(rs.getString("category"));
                t.setDescription(rs.getString("description"));
                t.setDate(rs.getString("date"));

                transactions.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    // NEW: insert a transaction and return generated id
    public int create(Transaction t) {
        int generatedId = -1;

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, t.getType());
            ps.setDouble(2, t.getAmount());
            ps.setString(3, t.getCategory());
            ps.setString(4, t.getDescription());
            ps.setString(5, t.getDate());
            
            // date string expected as "YYYY-MM-DD"
            Date sqlDate = Date.valueOf(t.getDate());  // converts String to java.sql.Date[web:101][web:102]
            ps.setDate(5, sqlDate);


            try (ResultSet rs = ps.executeQuery()) { // INSERT ... RETURNING id in PostgreSQL[web:100][web:101][web:106]
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    t.setId(generatedId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return generatedId;
    }

    // Small test main (optional)
    public static void main(String[] args) {
        TransactionDao dao = new TransactionDao();
        List<Transaction> list = dao.getAll();
        System.out.println("Found " + list.size() + " transactions:");
        for (Transaction t : list) {
            System.out.println(t);
        }
    }
}
