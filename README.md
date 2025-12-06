# 💰 Finance Tracker Backend

A lightweight backend for the Finance Tracker application designed using **Core Java (no Spring Boot)** and **PostgreSQL**.  
Built with a custom HTTP server using `ServerSocket`, demonstrating low-level networking, JDBC connectivity, and JSON handling.

---

## 🚀 Tech Stack

| Technology | Purpose |
|-----------|---------|
| **Java SE (Core Java)** | Backend logic & custom HTTP server |
| **JDBC** | Database connectivity |
| **PostgreSQL** | Data storage |
| **JSON (manual serialization)** | API data format |

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/health` | Check server status |
| `GET` | `/transactions` | Fetch all transactions |
| `POST` | `/transactions` | Create a new transaction (JSON body) |

Example POST JSON:
```json
{
  "type": "expense",
  "amount": 100.50,
  "category": "Food",
  "description": "Dinner",
  "date": "2025-01-01"
}
