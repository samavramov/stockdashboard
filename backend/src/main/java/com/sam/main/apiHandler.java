package com.sam.main;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class apiHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Predefined stock symbols the website will have
        String[] symbols = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"};

        // Only respond to GET requests
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        // Create a new databaseInteractions instance to fetch sentiments
        databaseInteractions db = new databaseInteractions();
        ArrayList<sentiment> sentiments = new ArrayList<>();

        // For each stock symbol, get the latest sentiment and add it to the sentiments list
        for (String symbol : symbols) {

            // This is where the database is queried for the latest sentiment
            ArrayList<sentiment> latest = db.getLatestSentimentsByStockSymbol(symbol, 1);

            // If there is a sentiment for this stock symbol, add it to the list
            if (!latest.isEmpty()) {
                sentiments.add(latest.get(0));
            }
        }

        // Serialize sentiments list to JSON
        String json = toJsonArray(sentiments);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET");
        // (Optional, if your client might send custom headers:)
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        // Write JSON to response
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String toJsonArray(ArrayList<sentiment> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        for (int i = 0; i < list.size(); i++) {
            sentiment s = list.get(i);
            sb.append("{");

            sb.append("\"stockSymbol\":\"").append(escapeJson(s.stockSymbol)).append("\",");
            sb.append("\"companyName\":\"").append(escapeJson(s.companyName)).append("\",");

            sb.append("\"sentimentValue\":").append(s.sentimentValue).append(",");

            // Format date as ISO string
            String isoDate = isoFormat.format(s.sentimentTimestamp);
            sb.append("\"sentimentTimestamp\":\"").append(escapeJson(isoDate)).append("\",");

            sb.append("\"url1\":\"").append(escapeJson(s.url1)).append("\",");
            sb.append("\"url2\":\"").append(escapeJson(s.url2)).append("\",");
            sb.append("\"url3\":\"").append(escapeJson(s.url3)).append("\",");

            sb.append("\"llmAnalysis\":\"").append(escapeJson(s.llmAnalysis)).append("\"");

            sb.append("}");
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    // Escape double quotes and backslashes in JSON strings
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"");
    }
}
