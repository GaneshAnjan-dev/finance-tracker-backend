package com.financeapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HealthServer {

    public static void main(String[] args) throws IOException {
        // Bind on all interfaces, port 9090
        ServerSocket server = new ServerSocket(9090, 50, java.net.InetAddress.getByName("0.0.0.0"));
        System.out.println("Server running at http://localhost:9090");
        System.out.println("Try:");
        System.out.println("  GET  /health");
        System.out.println("  GET  /transactions");
        System.out.println("  POST /transactions");

        while (true) {
            Socket client = server.accept();
            handleClient(client);
        }
    }

    private static void handleClient(Socket client) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            OutputStream rawOut = client.getOutputStream();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(rawOut, StandardCharsets.UTF_8), true)
        ) {
            // Request line
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                client.close();
                return;
            }

            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];

            // Read headers and capture Content-Length for POST
            String line;
            int contentLength = 0;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                String lower = line.toLowerCase();
                if (lower.startsWith("content-length:")) {
                    String[] headerParts = line.split(":");
                    if (headerParts.length == 2) {
                        contentLength = Integer.parseInt(headerParts[1].trim());
                    }
                }
            }

            // CORS preflight (OPTIONS)
            if ("OPTIONS".equals(method)) {
                sendJsonResponseWithStatus(out, 204, "");
                client.close();
                return;
            }

            if ("GET".equals(method) && "/health".equals(path)) {
                sendTextResponse(out, "OK");
            } else if ("GET".equals(method) && "/transactions".equals(path)) {
                handleGetTransactions(out);
            } else if ("POST".equals(method) && "/transactions".equals(path)) {
                handlePostTransaction(in, contentLength, out);
            } else {
                sendNotFound(out);
            }

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // GET /transactions
    private static void handleGetTransactions(PrintWriter out) {
        TransactionDao dao = new TransactionDao();
        List<Transaction> transactions = dao.getAll();
        String json = toJson(transactions);
        sendJsonResponse(out, json);
    }

    // POST /transactions
    private static void handlePostTransaction(BufferedReader in, int contentLength, PrintWriter out) throws IOException {
        // Read raw body
        char[] bodyChars = new char[contentLength];
        int read = in.read(bodyChars);
        String body = new String(bodyChars, 0, read);

        // Parse body JSON into Transaction
        Transaction t = parseTransactionFromJson(body);

        // Insert into DB
        TransactionDao dao = new TransactionDao();
        int newId = dao.create(t);

        if (newId > 0) {
            String json = toJsonSingle(t);
            sendJsonCreatedResponse(out, json); // HTTP 201
        } else {
            String errorJson = "{\"error\":\"Failed to create transaction\"}";
            sendJsonResponseWithStatus(out, 500, errorJson);
        }
    }

    // Very simple JSON -> Transaction parser (expects fixed keys)
    private static Transaction parseTransactionFromJson(String json) {
        Transaction t = new Transaction();

        t.setType(extractJsonString(json, "type"));
        String amountStr = extractJsonString(json, "amount");
        double amount = amountStr.isEmpty() ? 0.0 : Double.parseDouble(amountStr);
        t.setAmount(amount);
        t.setCategory(extractJsonString(json, "category"));
        t.setDescription(extractJsonString(json, "description"));
        t.setDate(extractJsonString(json, "date"));

        return t;
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int index = json.indexOf(pattern);
        if (index == -1) return "";

        int colon = json.indexOf(":", index);
        if (colon == -1) return "";

        int start = colon + 1;

        // Skip spaces
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        char firstChar = json.charAt(start);
        if (firstChar == '"') {
            // String value
            int endQuote = json.indexOf("\"", start + 1);
            if (endQuote == -1) return "";
            return json.substring(start + 1, endQuote);
        } else {
            // Number or bare value
            int end = start;
            while (end < json.length() && " ,}\r\n".indexOf(json.charAt(end)) == -1) {
                end++;
            }
            return json.substring(start, end);
        }
    }

    // JSON helpers

    private static String toJson(List<Transaction> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < list.size(); i++) {
            Transaction t = list.get(i);
            sb.append(toJsonSingle(t));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    private static String toJsonSingle(Transaction t) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(t.getId()).append(",");
        sb.append("\"type\":\"").append(escapeJson(t.getType())).append("\",");
        sb.append("\"amount\":").append(t.getAmount()).append(",");
        sb.append("\"category\":\"").append(escapeJson(t.getCategory())).append("\",");
        sb.append("\"description\":\"").append(escapeJson(t.getDescription())).append("\",");
        sb.append("\"date\":\"").append(escapeJson(t.getDate())).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"");
    }

    // HTTP response helpers with CORS

    private static void sendTextResponse(PrintWriter out, String body) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        out.print("HTTP/1.1 200 OK\r\n");
        out.print("Content-Type: text/plain; charset=utf-8\r\n");
        out.print("Access-Control-Allow-Origin: http://localhost:3000\r\n");
        out.print("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
        out.print("Access-Control-Allow-Headers: Content-Type\r\n");
        out.print("Content-Length: " + bytes.length + "\r\n");
        out.print("\r\n");
        out.print(body);
        out.flush();
    }

    private static void sendJsonResponse(PrintWriter out, String json) {
        sendJsonResponseWithStatus(out, 200, json);
    }

    private static void sendJsonCreatedResponse(PrintWriter out, String json) {
        // 201 Created
        sendJsonResponseWithStatus(out, 201, json);
    }

    private static void sendJsonResponseWithStatus(PrintWriter out, int statusCode, String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        out.print("HTTP/1.1 " + statusCode + " OK\r\n");
        out.print("Content-Type: application/json; charset=utf-8\r\n");
        out.print("Access-Control-Allow-Origin: http://localhost:3000\r\n");
        out.print("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
        out.print("Access-Control-Allow-Headers: Content-Type\r\n");
        out.print("Content-Length: " + bytes.length + "\r\n");
        out.print("\r\n");
        out.print(json);
        out.flush();
    }

    private static void sendNotFound(PrintWriter out) {
        String body = "Not Found";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        out.print("HTTP/1.1 404 Not Found\r\n");
        out.print("Content-Type: text/plain; charset=utf-8\r\n");
        out.print("Access-Control-Allow-Origin: http://localhost:3000\r\n");
        out.print("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
        out.print("Access-Control-Allow-Headers: Content-Type\r\n");
        out.print("Content-Length: " + bytes.length + "\r\n");
        out.print("\r\n");
        out.print(body);
        out.flush();
    }
}
