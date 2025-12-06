package com.financeapi;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("🚀 Simple server started on http://localhost:8080");
            System.out.println("✅ Test: http://localhost:8080/health");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String requestLine = in.readLine();
            
            String path = requestLine.split(" ")[1];
            
            String response;
            if (path.equals("/health")) {
                response = "FinanceAPI is running! 🤑";
            } else {
                response = "Welcome to FinanceAPI! Try /health";
            }
            
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/plain");
            out.println();
            out.println(response);
            
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
