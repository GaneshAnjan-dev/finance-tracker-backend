package com.financeapi;

public class Transaction {
    private int id;
    private String type;        // "income" or "expense"
    private double amount;
    private String category;
    private String description;
    private String date;        // "2025-01-01" format
    
    // Default constructor
    public Transaction() {}
    
    // Constructor with all fields
    public Transaction(int id, String type, double amount, String category, 
                      String description, String date) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    @Override
    public String toString() {
        return String.format("Transaction{id=%d, type='%s', amount=%.2f, category='%s', date='%s'}", 
                           id, type, amount, category, date);
    }
}
